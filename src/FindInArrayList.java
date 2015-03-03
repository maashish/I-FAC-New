
import java.util.ArrayList;
public class FindInArrayList {

    public static void main(String[] args) {
        ArrayList arrayList  = new ArrayList();
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

        FindInArrayList findInArrayListObject = new FindInArrayList();

        //search for 1
        findInArrayListObject.findInArrayList(arrayList, 1);

        //search for 4
        findInArrayListObject.findInArrayList(arrayList, 4);

        //search for 5
        findInArrayListObject.findInArrayList(arrayList, 5);

        //search for 7
        findInArrayListObject.findInArrayList(arrayList, 7);

        //search for 100
        findInArrayListObject.findInArrayList(arrayList, 100);
    }

    private void findInArrayList(ArrayList arrayList, int number) {
        int index;
        for(index = 0; index < arrayList.size(); index++) {
            if(((Integer) arrayList.get(index)) == number) {
                System.out.println("The first index where " + number + " was found is: " + index);
                return;
            }
        }
        System.out.println(number + " was NOT found in the given ArrayList");
        return;
    }
}
