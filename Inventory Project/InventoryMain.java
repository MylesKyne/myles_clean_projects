public class InventoryMain{
    
    private int inventory = 0; //sets up the inventory initially

    public InventoryMain(int add, int remove) //this is the main class, takes 2 parameters add and remove
    {
        Thread[] addThreads = new Thread[add]; //creates arrays of thread objects to add and remove items
        Thread[] removeThreads = new Thread[remove]; //read above

        for (int i = 0; i < add; i++) { //this starts thread for adding items
            addThreads[i] = new Thread(new Adds());
            addThreads[i].start();
        }


        for (int i = 0; i < remove; i++) { //starts the thread for removing items
            removeThreads[i] = new Thread(new Removes()); 
            removeThreads[i].start();
        }

        for (int i = 0; i < add; i++) { //this is the join method used to ensure threads have completed the task before we move to next line
            try {
                addThreads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        for (int i = 0; i < remove; i++) {//read above
            try {
                removeThreads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        System.out.println("Final inventory size = " + inventory); //displays the final inventory size at the end

    }


    

    public static void main(String[] args){
        if (args.length != 2) { //small piece of validation to ensure the user uses the correct format
            System.out.println("please use this usage, java InventoryMain <add> <remove>");
        }
        int add = Integer.parseInt(args[0]); //sets the first parameter as the add number
        int remove = Integer.parseInt(args[1]); //sets second parameter as the remove number
        
         InventoryMain inventoryMain = new InventoryMain(add, remove); //calls the InventoryMain function using add and remove, the parameters we already defined
 
     }
 

    
    public class Adds implements Runnable {

        public void run() {
            synchronized (InventoryMain.this){ //ensures only one thread can access and modify inventory at a time
                inventory++;
                System.out.println("Added. Inventory size = " + inventory);
            }
        }
    }

    public class Removes implements Runnable {

        public void run() {
            synchronized (InventoryMain.this){ //read above
                inventory--;
                System.out.println("Removed. Inventory size = " + inventory);
            }
        }
    } //this code now runs concurrently
}
    
    

