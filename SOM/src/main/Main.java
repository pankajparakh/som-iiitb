package main;

import java.util.ArrayList;


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
		som.saveSO("scormobj1","c:/scormcourse");
		//som.retriveXMLDoc("scormobj", "d:\\");
		som.retriveSO("scormobj","c:\\scormcourse_5511223");
		
		//som.checkInSO("scormobj1.zip","c:\\scormcourse.zip");
		
		//som.checkOutHeadSO("scormobj1.zip","c:\\scormcourse_CO.zip");
		
		//som.checkInSO("scormobj1.zip","c:\\scormcourse.zip");
		
		//som.checkInSO("scormobj.zip1","c:\\scormcourse.zip");
		
        //session.commit();
		ArrayList<String> list = new ArrayList<String>();
		list = som.getComponentsList("scormobj", "metadata");
		System.out.println(list.size());
		
		//som.saveXMLDoc("scormobj","c:\\scormcourse\\imsmanifest.xml");
		
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
