package com.codecanvas.visualizer;

import java.util.Arrays;

/**
 * Concrete visualizer subclass for Sorting Algorithms (Quick Sort, Merge Sort, Bubble Sort).
 */
public class SortingAlgorithmVisualizer extends AlgorithmVisualizer {

    private int[] array;
    private int comparisonCount;
    private int swapCount;

    public SortingAlgorithmVisualizer(String algorithmName, int[] initialArray) {
        super(algorithmName, "Sorting");
        this.array = initialArray != null ? Arrays.copyOf(initialArray, initialArray.length) : new int[]{34, 12, 89, 5, 23, 77, 45, 1};
        this.comparisonCount = 0;
        this.swapCount = 0;
    }

    @Override
    public void runSimulation() {
        logTrace.clear();
        stepCount = 0;
        comparisonCount = 0;
        swapCount = 0;
        long start = System.nanoTime();

        addTrace("Initial array: " + Arrays.toString(array));

        if (algorithmName.equalsIgnoreCase("Quick Sort")) {
            quickSort(0, array.length - 1);
        } else if (algorithmName.equalsIgnoreCase("Merge Sort")) {
            mergeSort(0, array.length - 1);
        } else {
            bubbleSort();
        }

        executionTimeNanos = System.nanoTime() - start;
        addTrace("Sorted array: " + Arrays.toString(array));
        addTrace("Total comparisons: " + comparisonCount + ", Total swaps: " + swapCount);
    }

    private void quickSort(int low, int high) {
        if (low < high) {
            int pi = partition(low, high);
            quickSort(low, pi - 1);
            quickSort(pi + 1, high);
        }
    }

    private int partition(int low, int high) {
        int pivot = array[high];
        addTrace("Selected pivot: " + pivot + " at index " + high);
        int i = (low - 1);

        for (int j = low; j < high; j++) {
            comparisonCount++;
            if (array[j] < pivot) {
                i++;
                swap(i, j);
            }
        }
        swap(i + 1, high);
        return i + 1;
    }

    private void mergeSort(int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSort(left, mid);
            mergeSort(mid + 1, right);
            merge(left, mid, right);
        }
    }

    private void merge(int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;
        int[] L = new int[n1];
        int[] R = new int[n2];
        System.arraycopy(array, left, L, 0, n1);
        System.arraycopy(array, mid + 1, R, 0, n2);

        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            comparisonCount++;
            if (L[i] <= R[j]) {
                array[k] = L[i++];
            } else {
                array[k] = R[j++];
            }
            k++;
        }
        while (i < n1) array[k++] = L[i++];
        while (j < n2) array[k++] = R[j++];
        addTrace("Merged subarray [" + left + " .. " + right + "]: " + Arrays.toString(array));
    }

    private void bubbleSort() {
        int n = array.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                comparisonCount++;
                if (array[j] > array[j + 1]) {
                    swap(j, j + 1);
                }
            }
        }
    }

    private void swap(int i, int j) {
        if (i == j) return;
        swapCount++;
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
        addTrace("Swapped elements at [" + i + "] <-> [" + j + "]: " + Arrays.toString(array));
    }

    @Override
    public String getExecutionSummary() {
        return String.format("[%s Visualizer] Array sorted in %d steps (%d comparisons, %d swaps, %.2f µs)",
                algorithmName, stepCount, comparisonCount, swapCount, executionTimeNanos / 1000.0);
    }
}
