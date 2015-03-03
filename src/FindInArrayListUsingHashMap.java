
import java.util.ArrayList;
import java.util.HashMap;
public class FindInArrayListUsingHashMap {

    public static void main(String[] args) {
        ArrayList arrayList  = new ArrayList();
        HashMap hashMap;
        arrayList.add(1);
        arrayList.add(1);
        arrayList.add(2);
        arrayList.add(2);
        arrayList.add(2);
        arrayList.add(3);
        arrayList.add(4);
        arrayList.add(4);
        arrayList.add(6);
        arrayList.add(7);

        FindInArrayListUsingHashMap findInArrayListUsingHashMapObject = new FindInArrayListUsingHashMap();

        hashMap = findInArrayListUsingHashMapObject.createHashMap(arrayList);

        //search for 1
        findInArrayListUsingHashMapObject.findInArrayList(arrayList, hashMap, 1);

        //search for 4
        findInArrayListUsingHashMapObject.findInArrayList(arrayList, hashMap, 4);

        //search for 5
        findInArrayListUsingHashMapObject.findInArrayList(arrayList, hashMap, 5);

        //search for 7
        findInArrayListUsingHashMapObject.findInArrayList(arrayList, hashMap, 7);

        //search for 100
        findInArrayListUsingHashMapObject.findInArrayList(arrayList, hashMap, 100);
    }

    private HashMap createHashMap(ArrayList arrayList) {
        HashMap hashMap = new HashMap();
        int index;
        for(index = 0; index < arrayList.size(); index++) {
            if(!hashMap.containsKey((Integer) arrayList.get(index))) {
                hashMap.put((Integer) arrayList.get(index), index);
            }
        }
        return hashMap;
    }

    private void findInArrayList(ArrayList arrayList, HashMap hashMap, int number) {
        int index;
        if (hashMap.containsKey(number)) {
            System.out.println("The first index where " + number + " was found is: " + hashMap.get(number));
            return;
        }
        System.out.println(number + " was NOT found in the given ArrayList");
        return;
    }
}
