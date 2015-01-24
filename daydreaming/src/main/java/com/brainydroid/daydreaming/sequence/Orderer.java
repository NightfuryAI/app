package com.brainydroid.daydreaming.sequence;

import android.annotation.SuppressLint;

import com.brainydroid.daydreaming.background.Logger;
import com.brainydroid.daydreaming.db.Util;
import com.google.inject.Inject;
import com.google.inject.Injector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Orderer<D extends BuildableOrderable<D,C>,C> {

    private static String TAG = "Orderer";

    @Inject Util util;
    @Inject Injector injector;
    @Inject BuildableOrder<D,C> buildableOrder;
    private int nSlots;

    public BuildableOrder<D,C> buildOrder(int nSlots, ArrayList<D> descriptions) {

        this.nSlots = nSlots;
        @SuppressLint("UseSparseArrays")
        HashMap<Integer,ArrayList<D>> map = new HashMap<Integer,ArrayList<D>>();

        // Place fixed groups
        putFixed(getFixed(descriptions), map);

        // Find which indices we still need to fill up
        ArrayList<Integer> remainingIndices = getRemainingIndices(map);
        int nFloatingToFill = remainingIndices.size();
        Logger.v(TAG, "Still have {} slots to fill", nFloatingToFill);

        // Get and place as many floating groups as there remain free slots
        putFloats(getRandomFloats(descriptions, nFloatingToFill), remainingIndices, map);

        // Shuffle groups internally, build the resulting order, and insert the afters
        buildableOrder.initialize(shuffleGroups(map), buildShuffledAftersMap(descriptions));

        return buildableOrder;
    }

    private ArrayList<ArrayList<D>> shuffleGroups(HashMap<Integer,ArrayList<D>> map) {
        ArrayList<ArrayList<D>> shuffledGroups = new ArrayList<ArrayList<D>>(nSlots);
        for (int i = 0; i < nSlots; i++) {
            shuffledGroups.add(null);
        }
        ArrayList<D> currentGroup;
        for (Map.Entry<Integer, ArrayList<D>> group : map.entrySet()) {
            Logger.v(TAG, "Shuffling slot {}", group.getKey());
            currentGroup = group.getValue();
            util.shuffle(currentGroup);
            shuffledGroups.set(group.getKey(), currentGroup);
        }

        return shuffledGroups;
    }

    private void putFixed(HashMap<Integer, ArrayList<D>> explicits,
                          HashMap<Integer, ArrayList<D>> map) {
        int originalIndex;
        int convertedIndex;
        for (Map.Entry<Integer, ArrayList<D>> explicitGroup : explicits.entrySet()) {
            originalIndex = explicitGroup.getKey();
            convertedIndex = originalIndex < 0 ?
                    nSlots + originalIndex : originalIndex;
            map.put(convertedIndex, explicitGroup.getValue());
        }
    }

    private HashMap<Integer,ArrayList<D>> getFixed(ArrayList<D> orderables) {
        @SuppressLint("UseSparseArrays")
        HashMap<Integer, ArrayList<D>> explicits = new HashMap<Integer, ArrayList<D>>();
        Integer position;

        for (D item : orderables) {
            if (item.getPosition().isFixed()) {
                position = item.getPosition().getFixedPosition();
                if (!explicits.containsKey(position)) {
                    explicits.put(position, new ArrayList<D>());
                }
                explicits.get(position).add(item);
            }
        }

        return explicits;
    }

    private void putFloats(ArrayList<ArrayList<D>> floats,
                           ArrayList<Integer> availablePositions,
                           HashMap<Integer,ArrayList<D>> map) {
        int n = floats.size();
        for (int i = 0; i < n; i++) {
            Logger.v(TAG, "Putting group {0} at slot {1}",
                    floats.get(i).get(0).getPosition(),
                    availablePositions.get(i));
            map.put(availablePositions.get(i), floats.get(i));
        }
    }

    private ArrayList<ArrayList<D>> getRandomFloats(ArrayList<D> orderables, int nFloats) {
        HashMap<String, ArrayList<D>> floats = new HashMap<String, ArrayList<D>>();
        String position;

        for (D item : orderables) {
            if (item.getPosition().isFloating()) {
                position = item.getPosition().getFloatingPosition();
                if (!floats.containsKey(position)) {
                    floats.put(position, new ArrayList<D>());
                }
                floats.get(position).add(item);
            }
        }

        return util.sample(new ArrayList<ArrayList<D>>(floats.values()), nFloats);
    }


    private ArrayList<Integer> getRemainingIndices(HashMap<Integer, ArrayList<D>> map) {
        ArrayList<Integer> remainingIndices = new ArrayList<Integer>();
        for (int i = 0; i < nSlots; i++) {
            if (!map.containsKey(i)) {
                remainingIndices.add(i);
            }
        }

        return remainingIndices;
    }

    private void _buildAftersTree(Node<D> node, HashSet<D> insertables) {
        Position position;
        Node<D> itemNode;
        for (D item : insertables) {
            position = item.getPosition();
            if (position.isAfter()) {
                if (position.getAfterPosition().equals(node.getData().getName())) {
                    itemNode = new Node<D>(item);
                    injector.injectMembers(itemNode);
                    _buildAftersTree(itemNode, insertables);
                    node.add(itemNode);
                }
            }
        }
    }

    private HashMap<String,ArrayList<Node<D>>> buildShuffledAftersMap(ArrayList<D> orderables) {
        Logger.v(TAG, "Recursively building afters map");

        HashMap<String,ArrayList<Node<D>>> aftersMap = new HashMap<String, ArrayList<Node<D>>>();
        HashSet<String> allAfterNames = new HashSet<String>();
        HashSet<D> allAfters = new HashSet<D>();

        // Build the list of all available afters and their names
        Position position;
        for (D item : orderables) {
            position = item.getPosition();
            if (position.isAfter()) {
                allAfters.add(item);
                allAfterNames.add(item.getName());
            }
        }

        // Find root afters, and build each of their trees
        for (D item : allAfters) {
            position = item.getPosition();
            if (!allAfterNames.contains(position.getAfterPosition())) {
                // This is a root after
                Node<D> nodeItem = new Node<D>(item);
                injector.injectMembers(nodeItem);
                _buildAftersTree(nodeItem, allAfters);
                if (aftersMap.containsKey(position.getAfterPosition())) {
                    aftersMap.get(position.getAfterPosition()).add(nodeItem);
                } else {
                    ArrayList<Node<D>> listItem = new ArrayList<Node<D>>();
                    listItem.add(nodeItem);
                    aftersMap.put(position.getAfterPosition(), listItem);
                }

            }
        }

        // Shuffle everything
        for (ArrayList<Node<D>> listItem : aftersMap.values()) {
            util.shuffle(listItem);
            for (Node<D> nodeItem : listItem) {
                nodeItem.shuffle();
            }
        }

        return aftersMap;
    }
}
