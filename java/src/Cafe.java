/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.*;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Cafe {

   //login info for later use
   private static String authorisedUser = null;

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Cafe
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Cafe (String dbname, String dbport) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://127.0.0.1:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Cafe

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
	 if(outputHeader){
	    for(int i = 1; i <= numCol; i++){
		System.out.print(rsmd.getColumnName(i) + "\t");
	    }
	    System.out.println();
	    outputHeader = false;
	 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
      // creates a statement object 
      Statement stmt = this._connection.createStatement (); 
 
      // issues the query instruction 
      ResultSet rs = stmt.executeQuery (query); 
 
      /* 
       ** obtains the metadata object for the returned result set.  The metadata 
       ** contains row and column info. 
       */ 
      ResultSetMetaData rsmd = rs.getMetaData (); 
      int numCol = rsmd.getColumnCount (); 
      int rowCount = 0; 
 
      // iterates through the result set and saves the data returned by the query. 
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>(); 
      while (rs.next()){
          List<String> record = new ArrayList<String>(); 
         for (int i=1; i<=numCol; ++i) 
            record.add(rs.getString (i)); 
         result.add(record); 
      }//end while 
      stmt.close (); 
      return result; 
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       if(rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current 
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();
	
	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 2) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Cafe.class.getName () +
            " <dbname> <port>");
         return;
      }//end if

      Greeting();
      Cafe esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Cafe object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         esql = new Cafe (dbname, dbport);

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              String user_type = find_type(esql);
	      switch (user_type){
		case "Customer": 
		  while(usermenu) {
                    System.out.println("MAIN MENU - Customer");
                    System.out.println("---------");
                    System.out.println("1. Browse Menu by ItemName");
                    System.out.println("2. Browse Menu by Type");
                    System.out.println("3. Add Order");
                    System.out.println("4. Update Order");
                    System.out.println("5. View Order History");
                    System.out.println("6. View Order Status");
                    System.out.println("7. Update User Info");
                    System.out.println(".........................");
                    System.out.println("9. Log out");
                      switch (readChoice()){
                       case 1: BrowseMenuName(esql); break;
                       case 2: BrowseMenuType(esql); break;
                       case 3: AddOrder(esql); break;
                       case 4: UpdateOrder(esql); break;
                       case 5: ViewOrderHistory(esql); break;
                       case 6: ViewOrderStatus(esql); break;
                       case 7: UpdateUserInfo(esql); break;
                       case 9: usermenu = false; break;
                       default : System.out.println("Unrecognized choice!"); break;
		      }//end switch
		  } break;
		case "Employee": 
		  while(usermenu) {
                    System.out.println("MAIN MENU - Employee");
                    System.out.println("---------");
                    System.out.println("1. Browse Menu by ItemName");
                    System.out.println("2. Browse Menu by Type");
                    System.out.println("3. Add Order");
                    System.out.println("4. Update Order");
                    System.out.println("5. View Current Orders");
                    System.out.println("6. View Order Status");
                    System.out.println("7. Update User Info");
                    System.out.println(".........................");
                    System.out.println("9. Log out");
                      switch (readChoice()){
                       case 1: BrowseMenuName(esql); break;
                       case 2: BrowseMenuType(esql); break;
                       case 3: AddOrder(esql); break;
                       case 4: EmployeeUpdateOrder(esql); break;
                       case 5: ViewCurrentOrder(esql); break;
                       case 6: ViewOrderStatus(esql); break;
                       case 7: UpdateUserInfo(esql); break;
                       case 9: usermenu = false; break;
                       default : System.out.println("Unrecognized choice!"); break;
		      }//end switch
		  } break;
		case "Manager ": 
		  while(usermenu) {
                    System.out.println("MAIN MENU - Manager");
                    System.out.println("---------");
                    System.out.println("1. Browse Menu by ItemName");
                    System.out.println("2. Browse Menu by Type");
                    System.out.println("3. Add Order");
                    System.out.println("4. Update Order");
                    System.out.println("5. View Current Orders");
                    System.out.println("6. View Order Status");
                    System.out.println("7. Update User Info");
                    System.out.println("8. Update Menu");
                    System.out.println(".........................");
                    System.out.println("9. Log out");
                      switch (readChoice()){
                       case 1: BrowseMenuName(esql); break;
                       case 2: BrowseMenuType(esql); break;
                       case 3: AddOrder(esql); break;
                       case 4: EmployeeUpdateOrder(esql); break;
                       case 5: ViewCurrentOrder(esql); break;
                       case 6: ViewOrderStatus(esql); break;
                       case 7: ManagerUpdateUserInfo(esql); break;
                       case 8: UpdateMenu(esql); break;
                       case 9: usermenu = false; break;
                       default : System.out.println("Unrecognized choice!"); break;
		      }//end switch
		  } break;
	      }//end switch
            }//end if
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface                         \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    **/
   public static void CreateUser(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();
         
	 String type="Customer";
	 String favItems="";

	 String query = String.format("INSERT INTO USERS (phoneNum, login, password, favItems, type) VALUES ('%s','%s','%s','%s','%s')", phone, login, password, favItems, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end
   
   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM Users WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

   public static String find_type(Cafe esql)
   {
       try
       {
           String query = String.format("SELECT U.type FROM Users U WHERE U.login = '%s'", authorisedUser );
           List<List<String>> userType = esql.executeQueryAndReturnResult(query);

           return userType.get(0).get(0);
       }
       catch(Exception e)
       {
           System.err.println (e.getMessage ());
           return null;
       }
   }

   public static void BrowseMenuName(Cafe esql)
   {
       try
       {
           String query = String.format("SELECT M.itemName FROM Menu M");
           int itemName = esql.executeQueryAndPrintResult(query);
       }
       catch(Exception e)
       {
           System.err.println (e.getMessage ());
       }
   }//end

   public static void BrowseMenuType(Cafe esql)
   {
       try
       {
           String query = String.format("SELECT DISTINCT M.type FROM Menu M");
           List<List<String>> menutypes= esql.executeQueryAndReturnResult(query);
           System.out.println();
           for(int i = 0 ; i < menutypes.size(); ++i)
           {
               System.out.println(" "  + i + ") " + menutypes.get(i).get(0));
           }
           System.out.println(" Select # for type you wish to browse: ");
           int choice = esql.readChoice();
           if(choice < 0 || choice >= menutypes.size())
           {
              System.out.println("Invalid Option");
           }
           else 
           {
              System.out.println("\n" + menutypes.get(choice).get(0) + "\n--------------------");
              query = String.format("SELECT M.itemName FROM Menu M WHERE M.type='" + menutypes.get(choice).get(0) + "'");
              esql.executeQueryAndPrintResult(query);
              System.out.println("--------------------\n");
           }

       }
       catch(Exception e)
       {
           System.err.println (e.getMessage ());
       }
   }//end

   public static Integer AddOrder(Cafe esql){
      try
      {
         Vector<String> orderNames = new Vector();
         Vector<Double> orderPrices = new Vector();
         String query = String.format("SELECT itemName, price FROM Menu");
         List<List<String>> itemLists = esql.executeQueryAndReturnResult(query);
         int numItems = itemLists.size();
         boolean moreitems = true;
         double orderPriceTotal = 0.00, numPrice = 0.00;
         String curItem = "", curPrice = "";
         System.out.println("\n\tCafe Menu");
         do
         {
            //pull menu items from query
            for(int i = 0; i < itemLists.size(); ++i)
            {
               curItem = itemLists.get(i).get(0); //get menu item name
               curPrice = itemLists.get(i).get(1); //get menu item price
               numPrice = Double.parseDouble(curPrice); //convert string to double
               numPrice = Math.round(numPrice * 100.0) / 100.0; //round to 2 decimals
               //System.out.print(i + ")   $" + numPrice + "    " + curItem + "\n"); // display menu items
               System.out.println(String.format("%d)   $%-6.2f    %s", i, numPrice, curItem)); 
            }
            
            System.out.print("\nItems in Cart: ");
            for(int i = 0;i < orderNames.size(); ++i)
            {
               System.out.print(orderNames.get(i) + "  ");
            }
            System.out.println(String.format("\nOrder Total: $%.2f", orderPriceTotal));
            System.out.print("\nEnter item number to add it to the order -- OR -- Enter " + numItems + " to checkout: ");
            int itemChoice = Integer.parseInt(in.readLine());
            if(itemChoice < numItems)
            { 
               //add item to order vector
               orderNames.add(itemLists.get(itemChoice).get(0).trim().replaceAll(" +", " ")); //trim elims extra spaces
               //add price of chosen item to item total
               curPrice = itemLists.get(itemChoice).get(1);
               numPrice = Double.parseDouble(curPrice);
               numPrice = Math.round(numPrice * 100.0) / 100.0;
               //add price to order vector
               orderPrices.add(numPrice);
               orderPriceTotal += numPrice;
               orderPriceTotal = Math.round(orderPriceTotal * 100.0) / 100.0;
               moreitems = true;
            }
            else if(itemChoice == numItems)
            {
               moreitems = false;
            }
            else
               System.out.println("INVALID Entry!");
         }while(moreitems);
         if(orderNames.size() > 0)
         {
            System.out.println("items in this order: ");
            for(int i = 0; i < orderNames.size(); ++i)
            {
               System.out.print(orderNames.get(i) + " $" + orderPrices.get(i) + "\n");
            }
               System.out.print("Order total:   $" + orderPriceTotal + "\n");
               System.out.println("Confirm order?  0)yes    1) no");
               int confOrder = Integer.parseInt(in.readLine());
            if(confOrder == 0)
            {
               //insert order and items into databases
               boolean hasPaid = false;
               query = String.format("INSERT INTO ORDERS (login, paid, timeStampRecieved, total) VALUES ('"+ authorisedUser +"', "+ hasPaid +", now()::timestamp, "+ orderPriceTotal +")");
               //List<List<String>> qryResult= esql.executeQueryAndReturnResult(query);
               esql.executeUpdate(query);
               //System.out.println("Order placed");                  
               query = String.format("SELECT MAX(orderid) FROM Orders O WHERE O.login ='" + authorisedUser + "' AND paid = false" );
               List<List<String>> orderIDquery= esql.executeQueryAndReturnResult(query);
               String oid = orderIDquery.get(0).get(0);
               //insert menu items for order
               String statusDefault = "order processing", commentsDefault = "thank you for your order";
               for(int i = 0; i < orderNames.size(); ++i)
               {
                  query = String.format("INSERT INTO ItemStatus (orderid, itemName, lastUpdated, status, comments) VALUES ("+ oid+", '"+ orderNames.get(i) +"', now()::timestamp, '"+statusDefault+"', '"+ commentsDefault +"' )");
                  esql.executeUpdate(query);
               }
               System.out.println("Order Placed Successfully");
            }
            else
            {
               System.out.println("Order Cancelled");
            }
         }
         else
         {
            System.out.println("No items chosen, order cancelled.");
         }

      }
      catch(Exception e)
      {
         System.err.println(e.getMessage());
         return null;
      }
      Integer orderid=0;
      return orderid;
   }//end 

   public static void UpdateOrder(Cafe esql){
      try
      {
         System.out.print("\n Enter Order ID for the order you wish to update:  \n");
         String oidstring = esql.in.readLine();
         int oid = Integer.parseInt(oidstring);
         String query = String.format("SELECT * FROM Orders O WHERE O.login ='" + authorisedUser+"' AND O.paid = false AND O.orderid ="+ oid );
         List<List<String>> orderquery= esql.executeQueryAndReturnResult(query);
         oidstring = orderquery.get(0).get(0);
         oid = Integer.parseInt(oidstring);
         boolean cont = true;
         do
         {
            query = String.format("SELECT * FROM ItemStatus I WHERE I.orderid = " + oid);
            List<List<String>> itemquery= esql.executeQueryAndReturnResult(query);
            System.out.println("Order #: " + oid);
            query = String.format("SELECT total FROM Orders WHERE orderid = " + oid);
            for(int j = 0; j < itemquery.size(); ++j)
            {
              System.out.print(j + ") " + itemquery.get(j).get(1).trim().replaceAll(" +", " ")+ " " + itemquery.get(j).get(2) + "\n");
            }
            List<List<String>> totalquery = esql.executeQueryAndReturnResult(query);
            String oTotalString = totalquery.get(0).get(0);
            double oTotal = Double.parseDouble(oTotalString);
            oTotal = Math.round(oTotal * 100.0) / 100.0;
            System.out.println("Total cost: $" + oTotal);
            System.out.print(" Enter number of item to edit OR\n '" + itemquery.size() + "' to add item OR \n '" + (itemquery.size() + 1) + "' to finish\n");
            int numItem = esql.readChoice();
            if(numItem == itemquery.size()+1)// finished
            {
               cont = false;
            }
            else if(numItem == itemquery.size()) //add item
            {
                String statusDefault = "order processing", commentsDefault = "thank you for your order";
                query = String.format("SELECT itemName, price FROM Menu");
                  List<List<String>> itemLists = esql.executeQueryAndReturnResult(query);
                  for(int i = 0; i < itemLists.size(); ++i)
                  {
                     String curItem = itemLists.get(i).get(0); //get menu item name
                     String curPrice = itemLists.get(i).get(1); //get menu item price
                     double numPrice = Double.parseDouble(curPrice); //convert string to double
                     numPrice = Math.round(numPrice * 100.0) / 100.0; //round to 2 decimals
                     System.out.println(String.format("%d)   $%-6.2f    %s", i, numPrice, curItem)); 
                  }
                  System.out.println("Enter Item Number of item you wish to add");
                  int newItemNum = esql.readChoice();
                  String newName = itemLists.get(newItemNum).get(0);
                  String newPriceString = itemLists.get(newItemNum).get(1);
                  double newPrice = Double.parseDouble(newPriceString);
                  newPrice = Math.round(newPrice * 100.0) / 100.0;
                  query = String.format("INSERT INTO ItemStatus (orderid, itemName, lastUpdated, status, comments) VALUES ("+ oid +", '"+ newName +"', now()::timestamp, '"+statusDefault+"', '"+ commentsDefault +"' )");
                  esql.executeUpdate(query);
                  query = String.format("UPDATE Orders SET (total) = (total + " + newPrice + ") WHERE orderid =" + oid); 
                  esql.executeUpdate(query);

            }
            else if(numItem < itemquery.size() && numItem >= 0) //edit item
            {
               String itemName = itemquery.get(numItem).get(1);
               query = String.format("SELECT M.price FROM Menu M WHERE M.itemName = '" + itemName + "'");
               List<List<String>> pricequery = esql.executeQueryAndReturnResult(query);
               String itemCostString = pricequery.get(0).get(0);
               double itemCost = Double.parseDouble(itemCostString);
               itemCost = Math.round(itemCost * 100.0) / 100.0;
               System.out.println("0) swap item  1) remove item");
               int numAction = esql.readChoice();
               if(numAction == 0)
               {
                  query = String.format("SELECT itemName, price FROM Menu");
                  List<List<String>> itemLists = esql.executeQueryAndReturnResult(query);
                  for(int i = 0; i < itemLists.size(); ++i)
                  {
                     String curItem = itemLists.get(i).get(0); //get menu item name
                     String curPrice = itemLists.get(i).get(1); //get menu item price
                     double numPrice = Double.parseDouble(curPrice); //convert string to double
                     numPrice = Math.round(numPrice * 100.0) / 100.0; //round to 2 decimals
                     System.out.println(String.format("%d)   $%-6.2f    %s", i, numPrice, curItem)); 
                  }
                  System.out.println("Enter New Item Number");
                  int newItemNum = esql.readChoice();
                  String newName = itemLists.get(newItemNum).get(0);
                  String newPriceString = itemLists.get(newItemNum).get(1);
                  double newPrice = Double.parseDouble(newPriceString);
                  newPrice = Math.round(newPrice * 100.0) / 100.0;
                  query = String.format("UPDATE ItemStatus SET (itemName, lastUpdated) = ('" + newName + "',now()::timestamp) WHERE orderid = " + oid + " AND itemName = '" + itemName + "'");
                  esql.executeUpdate(query);
                  double priceModifier = newPrice-itemCost; //new cost - old cost
                  query = String.format("UPDATE Orders SET (total) = (total + " + priceModifier + ") WHERE orderid =" + oid); 
                  esql.executeUpdate(query);
               }
               else if(numAction == 1)
               {
                  //delete item and update order totalprice
                  if(itemquery.size() == 1) //when down to last item, if deleted, remove order completely
                  {
                     query = String.format("DELETE FROM ItemStatus WHERE orderid = " + oid + " AND itemName = '" + itemName + "'");
                     esql.executeUpdate(query);
                     query = String.format("DELETE FROM Orders WHERE orderid = " + oid);
                     esql.executeUpdate(query);
                     System.out.println("Entire Order Deleted, last item removed");
                     cont = false;
                  }
                  else if(itemquery.size() > 1)
                  {
                     query = String.format("DELETE FROM ItemStatus WHERE orderid = " + oid + " AND itemName = '" + itemName + "'");
                     esql.executeUpdate(query); 
                     query = String.format("UPDATE Orders SET (total) = (total - " + itemCost + ") WHERE orderid =" + oid); 
                     esql.executeUpdate(query);

                     System.out.println("Item Removed");
                  }
               }
               else
               {
                  System.out.println("Invalid Entry");
               }
            }
            else
            {
               System.out.println("Invalid Entry");
            }
         }while(cont == true);
      }
      catch(Exception e)
      {
         System.err.println (e.getMessage() );
      }
   }//end

   public static void EmployeeUpdateOrder(Cafe esql){
      try
      {
         System.out.println(" 0) update personal order \n 1) Update order paid \n 2) Update item status");
         int choice = esql.readChoice();
         if(choice == 0)
         {
            UpdateOrder(esql);
         }
         else if(choice == 1)
         {
            System.out.println("Enter an Orderid to set it to paid");
            String oidstring = esql.in.readLine();
            int oid = Integer.parseInt(oidstring);
            String query = String.format("UPDATE Orders SET paid = true WHERE orderid =" + oid); 
            esql.executeUpdate(query);
         }
         else if(choice == 2)
         {
            System.out.println("Enter an Orderid");
            String oidstring = esql.in.readLine();
            int oid = Integer.parseInt(oidstring);
            System.out.println("Enter the Item Name");
            String itemstring = esql.in.readLine();
            String query = String.format("SELECT status FROM ItemStatus WHERE orderid = " + oid + " AND itemName = '" + itemstring + "'");
            esql.executeQueryAndPrintResult(query);

            System.out.println("Enter new status: ");
            String statusString = esql.in.readLine();
            query = String.format("UPDATE ItemStatus SET status = '" + statusString + "' WHERE orderid = " + oid + " AND itemName = '" + itemstring + "'");
            esql.executeUpdate(query);
            System.out.println("Status Updated!");
            query = String.format("SELECT status FROM ItemStatus WHERE orderid = " + oid + " AND itemName = '" + itemstring + "'");
            esql.executeQueryAndPrintResult(query);
         }
         else
         {
            System.out.println("Invalid Option");
         }
      }
      catch(Exception e)
      {
         System.err.println (e.getMessage() );
      }
   }//end

   public static void ViewOrderHistory(Cafe esql){
      try
      {
         String query = String.format("SELECT O.orderid, O.total FROM Orders O WHERE O.login ='" + authorisedUser +"' AND O.paid = false ORDER BY orderid DESC LIMIT 5" );
         List<List<String>> orderIDquery= esql.executeQueryAndReturnResult(query);
         for(int i = 0; i < 5; ++i)
         {
            String oidstring = orderIDquery.get(i).get(0);
            String orderTotal= orderIDquery.get(i).get(1);
            double numTotal = Double.parseDouble(orderTotal); //convert string to double
            int oid = Integer.parseInt(oidstring);
            query = String.format("SELECT * FROM ItemStatus I WHERE I.orderid = " + oid);
            List<List<String>> itemquery= esql.executeQueryAndReturnResult(query);
            System.out.println("Order #: " + oid);
            for(int j = 0; j < itemquery.size(); ++j)
            {   
              System.out.print(itemquery.get(j).get(1).trim().replaceAll(" +", " ")+ " " + itemquery.get(j).get(2) + "\n"); 
            }   
            System.out.println(String.format("Total: $%.2f", numTotal)); 
         }   
      }
      catch(Exception e)
      {
         System.err.println (e.getMessage());
      }
   }//end

   public static void UpdateUserInfo(Cafe esql)
   {

        String currentUser = authorisedUser;
        boolean getChoice = true;

        while(getChoice)
        {
            System.out.println("Update User Information");
            System.out.println("1. Update Password");
            System.out.println("2. Update Phone Number");
            System.out.println("3. Update Favorite Items");
            System.out.println("4. Return to Main Menu");

            switch(esql.readChoice())
            {
                case 1: UpdatePassword(esql, currentUser);
                        break;
                case 2: UpdatePhoneNumber(esql, currentUser);
                        break;
                case 3: UpdateFavItems(esql, currentUser);
                        break;
                case 4: getChoice = false;
                        break;

            }
        }

   }//end

   public static void UpdatePassword(Cafe esql, String currentUser)
   {
       String password;
       
       try
       {
           System.out.print("\n Enter new password \n");
           password = esql.in.readLine();
           String query = String.format("UPDATE users set password = '%s' WHERE login = '"+currentUser+"' ", password);
           esql.executeUpdate(query);
       }
       catch (Exception e)
       {
           System.err.println (e.getMessage());
       }

   } //end updatePassword helper


   public static void UpdatePhoneNumber(Cafe esql, String currentUser)
   {
       String number;
       
       try
       {
           System.out.print("\n Enter new phone number \n");
           number = esql.in.readLine();
           String query = String.format("UPDATE users set phoneNum = '%s' WHERE login = '"+currentUser+"' ", number);
           esql.executeUpdate(query);
       }
       catch (Exception e)
       {
           System.err.println (e.getMessage());
       }

   } //end updatePhoneNumber helper


   public static void UpdateFavItems(Cafe esql, String currentUser)
   {
       String items;
       
       try
       {
           System.out.print("\n Enter new items \n");
           items = esql.in.readLine();
           String query = String.format("UPDATE users set favItems = '%s' WHERE login = '"+currentUser+"' ", items);
           esql.executeUpdate(query);
       }
       catch (Exception e)
       {
           System.err.println (e.getMessage());
       }

   } //end updateFavItems helper

 
   public static void UpdateType(Cafe esql, String currentUser)
   {
       String type;
       
       try
       {
           System.out.print("\n Enter User Type to be changed \n");
           type = esql.in.readLine();
           String query = String.format("UPDATE users set type = '%s' WHERE login = '"+currentUser+"' ", type);
           esql.executeUpdate(query);
       }
       catch (Exception e)
       {
           System.err.println (e.getMessage());
       }

   } //end updateFavItems helper  


   public static void ManagerUpdateUserInfo(Cafe esql)
   {
        String currentUser = authorisedUser;
        String editUser;
        boolean getChoice = true;
        
        System.out.println("\tEnter User to Edit");
        editUser = System.console().readLine();
 

        while(getChoice)
        {
            System.out.println("Update User Information");
            System.out.println("1. Update Password");
            System.out.println("2. Update Phone Number");
            System.out.println("3. Update Favorite Items");
            System.out.println("4. Update Type");
            System.out.println("5. Return to Main Menu");

            switch(esql.readChoice())
            {
                case 1: UpdatePassword(esql, editUser);
                        break;
                case 2: UpdatePhoneNumber(esql, editUser);
                        break;
                case 3: UpdateFavItems(esql, editUser);
                        break;
                case 4: UpdateType(esql, editUser);
                        break;
                case 5: getChoice = false;
                        break;

            }
        }

   }//end

   public static void UpdateMenu(Cafe esql){
      // Your code goes here.
      // ...
      // ...
   }//end

   public static void ViewOrderStatus(Cafe esql)
   {
       String orderId;
       
       try
       {
           
         System.out.println("\tEnter Order Id");
         orderId = esql.in.readLine();
         String query = String.format("SELECT * FROM ItemStatus WHERE orderid = '%s' ", orderId);

         esql.executeQueryAndPrintResult(query);
       }
       catch (Exception e)
       {
           System.err.println (e.getMessage());
       }
    
   }//end

   public static void ViewCurrentOrder(Cafe esql){
      try
      {
         String query = String.format("SELECT O.orderid, O.total, O.login FROM Orders O WHERE O.paid = false AND O.timeStampRecieved >= NOW() - '1 day'::INTERVAL" );
         List<List<String>> orderIDquery= esql.executeQueryAndReturnResult(query);
         for(int i = 0; i < orderIDquery.size(); ++i)
         {
            String oidstring = orderIDquery.get(i).get(0);
            String orderTotal= orderIDquery.get(i).get(1);
            String orderLogin = orderIDquery.get(i).get(2);
            double numTotal = Double.parseDouble(orderTotal); //convert string to double
            int oid = Integer.parseInt(oidstring);
            query = String.format("SELECT * FROM ItemStatus I WHERE I.orderid = " + oid);
            List<List<String>> itemquery= esql.executeQueryAndReturnResult(query);
            System.out.println("Order #: " + oid);
            for(int j = 0; j < itemquery.size(); ++j)
            {
               System.out.print(itemquery.get(j).get(1).trim().replaceAll(" +", " ")+ " " + itemquery.get(j).get(2) + "\n"); 
            }
            System.out.println(String.format("Total: $%.2f     Customer: %s", numTotal, orderLogin)); 

         }

      }
      catch (Exception e)
      {
          System.err.println (e.getMessage());
      }
   }//end

   public static void Query6(Cafe esql){
      // Your code goes here.
      // ...
      // ...
   }//end Query6

}//end Cafe
