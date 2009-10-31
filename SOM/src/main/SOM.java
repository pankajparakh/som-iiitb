/**
 * 
 */
package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveBlobNodeIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.versioning.interfaces.XhiveVersionIf;

/**
 * @author Pankaj
 *
 */
public class SOM {
	XhiveDriverIf driver;
	XhiveSessionIf session;
	XhiveDatabaseIf dbhandle;
	XhiveLibraryIf rootLibrary;
	String libraryName = "scorm-objects";
	String manifestlibrary = "manifests";
	public boolean dbConnectionInit(String adminname,String pass,String dbname)
	{
		driver = XhiveDriverFactory.getDriver();
		driver.init();
		session = driver.createSession();
		try {

			// open a connection to the database
			session.connect(adminname, pass, dbname);
			session.begin();
			dbhandle = session.getDatabase();
			rootLibrary = dbhandle.getRoot();

			return true;
		} catch (Exception e) {
			System.out.println("ConnectDatabase failed: ");
			e.printStackTrace();
		}
		return false;
	}

	public boolean dbCommit()
	{
		try {
			session.commit();
			return true;

		}catch(Exception ex )
		{
			System.out.println("Unable to commit:");
			ex.printStackTrace();
		}
		finally
		{
			if (session.isOpen()) {
				session.rollback();
			}
			if (session.isConnected()) {
				session.disconnect();
			}
			driver.close();
		}
		return false;
	}

	public boolean saveSO(String name,String path)
	{
		//TODO:
		try
		{
			XhiveLibraryIf soLibrary = (XhiveLibraryIf)rootLibrary.get(libraryName);
			XhiveBlobNodeIf so =  soLibrary.createBlob();
			so.setName(name);
			so.setContents(new FileInputStream(path));
			soLibrary.appendChild(so);
			//System.out.println(soLibrary.getName());
			return true;

		}catch(Exception ex)
		{
			System.out.println("SOM.saveSO:Error - "+ex.getMessage());
		}

		return false;
	}

	public boolean retriveSO(String name,String path)
	{
		try
		{
			XhiveLibraryIf soLibrary = (XhiveLibraryIf)rootLibrary.get(libraryName);
			XhiveBlobNodeIf so =  (XhiveBlobNodeIf)soLibrary.get(name);
			//			so.setName(name);
			//			so.setContents(new FileInputStream(path));
			//			soLibrary.appendChild(so);
			InputStream in = so.getContents();
			FileOutputStream out = new FileOutputStream(path);
			int sosize = (int)so.getSize();
			byte[] buffer = new byte[sosize];
			int length;
			while((length = in.read(buffer)) != -1) {
				out.write(buffer, 0, length);
			}

			//System.out.println(so.getName());
			return true;

		}catch(Exception ex)
		{
			System.out.println("SOM.retriveSO:Error - "+ex.getMessage());
		}

		return false;
	}

	public boolean checkOutHeadSO(String name,String path)
	{
		try
		{
			XhiveLibraryIf soLibrary = (XhiveLibraryIf)rootLibrary.get(libraryName);
			XhiveBlobNodeIf so = (XhiveBlobNodeIf)soLibrary.get(name);
			if(so == null)
			{
				System.out.println("SOM.checkOutHeadSO:Error - Object Not Found");
				return false;
			}

			XhiveVersionIf version = so.getXhiveVersion();
			if(version != null)
			{
				XhiveBlobNodeIf soout =  (XhiveBlobNodeIf)version.checkOutLibraryChild();
				InputStream in = soout.getContents();
				FileOutputStream out = new FileOutputStream(path);
				int sosize = (int)soout.getSize();
				byte[] buffer = new byte[sosize];
				int length;
				while((length = in.read(buffer)) != -1) {
					out.write(buffer, 0, length);
				}
				System.out.println("SOM.checkOutHeadSO:Version info" + version.getId());
				
			}
			else
			{
				System.out.println("SOM.checkOutHeadSO:Error - Object Found, Not Versionable");
				return false;
			}
			return true;

		}catch(Exception ex)
		{
			System.out.println("SOM.checkInSO:Error - "+ex.getMessage());
		}

		return false;
	}

	public boolean checkInSO(String name,String path)
	{
		try
		{
			XhiveLibraryIf soLibrary = (XhiveLibraryIf)rootLibrary.get(libraryName);
			XhiveBlobNodeIf so = (XhiveBlobNodeIf)soLibrary.get(name);
			if(so != null)
			{

			}
			else
			{
				so =  soLibrary.createBlob();
				so.setName(name);
				so.setContents(new FileInputStream(path));
				so.makeVersionable();
				soLibrary.appendChild(so);
				return true;
				//System.out.println(soLibrary.getName());
			}

			XhiveVersionIf version = so.getXhiveVersion();
			if(version != null)
			{
				XhiveBlobNodeIf sonew =  soLibrary.createBlob();
				sonew.setContents(new FileInputStream(path));
				//version.checkOutLibraryChild();
				version.checkInLibraryChild(sonew);
				System.out.println("SOM.checkInSO:Version info" + version.getId());
			}
			else
			{
				System.out.println("SOM.checkInSO:Error - Object Found, Not Versionable");
				return false;
			}
			return true;

		}catch(Exception ex)
		{
			System.out.println("SOM.checkInSO:Error - "+ex.getMessage());
		}

		return false;
	}
	
	public ArrayList<String> getComponentsList(String fileneme,String comptype)
	{
		ArrayList<String> ret = new ArrayList<String>();
		try {
			
			File file = new File(fileneme);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			//System.out.println("Root element " + doc.getDocumentElement().getNodeName());
			NodeList nodeLst = doc.getElementsByTagName("resource");
			//System.out.println("Information of all employees - "+  nodeLst.getLength());
			
			for (int s = 0; s < nodeLst.getLength(); s++) {

				Node fstNode = nodeLst.item(s);

				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

					Element fstElmnt = (Element) fstNode;
					if(fstElmnt.getAttribute("adlcp:scormtype").equals(comptype))
						ret.add(fstElmnt.getAttribute("href"));
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public void test()
	{
		//session.begin();
		XhiveLibraryIf library = session.getDatabase().getRoot(); session.commit(); session.begin();
		library = session.getDatabase().getRoot(); System.out.println(library.getName());
	}

}
