package edu.spbu.sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by artemaliev on 07/09/15.
 */
public class IntSort {
    public static void Merge(int arr[], int l, int m, int r){
        int size1 = m - l + 1, size2 = r - m;
        int fir = 0, sec = 0;
        int Larr[] = new int [size1]; int Rarr[] = new int [size2];
        //System.arraycopy(arr, l, Larr, 0, size1);
        //System.arraycopy(arr, m + 1, Rarr, 0, size2);
        for(int i = 0; i < size1; i++){
            Larr[i] = arr[i + l];
        }
        for(int i = 0; i < size2; i++){
            Rarr[i] = arr[i + m + 1];
        }
        int k = l;
        while(fir < Larr.length && sec < Rarr.length) {
            if (Larr[fir] > Rarr[sec]) {
                //System.arraycopy(Rarr, sec++, arr, k + l, 1 );
                arr[k] = Rarr[sec];
                sec++;
            } else {
                //System.arraycopy(Larr, fir++, arr, k + l, 1);
                arr[k] = Larr[fir];
                fir++;
            }
            k++;
        }
        while(fir < size1){
            //System.arraycopy(Larr, fir++, arr, k + l, 1);
            arr[k] = Larr[fir];
            fir++;
            k++;
        }
        while(sec < size2){
            //System.arraycopy(Rarr, sec++, arr, k + l, 1 );
            arr[k] = Rarr[sec];
            sec++;
            k++;
        }

    }

    public static void MergeSort(int[] arr, int l, int r){
        while(l < r) {
            int m = r - (r + l) / 2;
            MergeSort(arr, l, m - 1);
            MergeSort(arr, m, r);
            Merge(arr, l, m, r);
        }
    }

    public static void sort (int array[]) {
    MergeSort(array, 0, array.length - 1);
  }

  public static void sort (List<Integer> list) {
    Collections.sort(list);
  }
}
