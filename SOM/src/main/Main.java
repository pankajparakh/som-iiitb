package main;


public class Main {
	public static void main(String argv[])
	{
		SOM som = new SOM();
		boolean ret = som.dbConnectionInit("Administrator", "admin", "SODB");
		if(!ret)
		{
			System.out.println("Main:Error in connection");
			System.exit(1);
		}
		else
		{
			System.out.println("Main:Connected Successfully");
		}
		//som.test();
		som.saveSO("scormobj.zip","c:\\scormcourse.zip");
		som.retriveSO("scormobj.zip","c:\\scormcourse_1.zip");
		
		//som.checkInSO("scormobj1.zip","c:\\scormcourse.zip");
		
		som.checkOutHeadSO("scormobj1.zip","c:\\scormcourse_CO.zip");
		
		som.checkInSO("scormobj1.zip","c:\\scormcourse.zip");
		
		//som.checkInSO("scormobj.zip1","c:\\scormcourse.zip");
		
        //session.commit();
		ret = som.dbCommit();
		if(ret)
		{
			System.out.println("Main:Successfully commited");
		}
		else
		{
			System.out.println("Main:Commit failed");
			System.exit(1);
		}
	}
}
