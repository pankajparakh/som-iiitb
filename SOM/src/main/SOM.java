/**
 * 
 */
package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import org.w3c.dom.Document;
import org.w3c.dom.ls.LSParser;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveBlobNodeIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.dom.interfaces.XhiveNodeIf;
import com.xhive.versioning.interfaces.XhiveVersionIf;


import com.xhive.query.interfaces.XhiveXQueryValueIf;
import com.xhive.util.interfaces.IterableIterator;

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
	String assetlibrary = "assets";
	String manifestlibrary = "manifests";
	String metalibrary = "som-metadata";
	String otherslibrary = "others";
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
		String rootpath = "";
		//TODO: extract the zip file and set its root folder path in rootpath variable		
		rootpath = path;
		ArrayList<String> assetlist;// = getComponentsList(name, "asset");
		try
		{
			XhiveLibraryIf lib = (XhiveLibraryIf)rootLibrary.get(manifestlibrary);
			if (!saveXMLDoc(name, rootpath+"/imsmanifest.xml",lib))
			{
				System.out.println("SOM.saveSO:Error");
				return false;
			}
			assetlist = getComponentsList(name, "asset");
			for(int i=0;i<assetlist.size();i++)
			{
				//TODO:Generate unique name
				UUID uid = UUID.randomUUID();
				//TODO:Add to asset-meta.xml
				int temp = assetlist.get(i).lastIndexOf('/');
				String location = (assetlist.get(i)).substring(0, temp);
				String realname = (assetlist.get(i)).substring(temp+1);
				String insqry = "<asset xmlns='http://www.iiitb.ac.in/docengg'> \n"
					+"<object>"+ name +"</object>\n"
					+"<location>"+ location +"</location>\n"
					+"<realname>"+ realname +"</realname>\n"
					+"<uname>"+ name + "_" + uid.toString() +"</uname>\n"
					+"</asset>\n";
				XhiveLibraryIf soLibrary = (XhiveLibraryIf)rootLibrary.get(metalibrary);
				Document doc = (Document)soLibrary.get("asset-meta.xml");
//				System.out.println(doc.toString());
				IterableIterator result = soLibrary.executeXQuery(insqry, (XhiveDocumentIf)doc);
				// We know this query will only return a single node.
				XhiveXQueryValueIf value = (XhiveXQueryValueIf)result.next();
				XhiveNodeIf node = value.asNode();
				//node.set
				// Append it to the document element of destination document
				doc.getDocumentElement().appendChild(node);


				//TODO:Add to scormobj-meta.xml
				insqry = "<asset xmlns='http://www.iiitb.ac.in/docengg'> \n"
					+"<uname>"+ name + "_" + uid.toString() +"</uname>\n"
					+"<realobject>"+ name +"</realobject>\n"
					+"<location>"+ location +"</location>\n"
					+"<realname>"+ realname +"</realname>\n"
					+"</asset>\n";
				soLibrary = (XhiveLibraryIf)rootLibrary.get(metalibrary);
				doc = (Document)soLibrary.get("scormobj-meta.xml");
//				System.out.println(doc.toString());
				result = soLibrary.executeXQuery(insqry, (XhiveDocumentIf)doc);
				// We know this query will only return a single node.
				value = (XhiveXQueryValueIf)result.next();
				node = value.asNode();
				// Append it to the document element of destination document
				doc.getDocumentElement().appendChild(node);
				
				
				if(!saveBlob(name + "_" + uid.toString(), rootpath + "/" +assetlist.get(i)))
				{
					System.out.println("SOM.saveSO:Error 1");
					return false;
				}
				
			}
			ArrayList<String> metalist = getComponentsList(name, "metadata");
			for(int i=0;i<metalist.size();i++)
			{
				UUID uid = UUID.randomUUID();
				//TODO:Add to asset-meta.xml
				int temp = assetlist.get(i).lastIndexOf('/');
				String location = (metalist.get(i)).substring(0, temp);
				String realname = (metalist.get(i)).substring(temp+1);
				String insqry = "<asset xmlns='http://www.iiitb.ac.in/docengg'> \n"
					+"<object>"+ name +"</object>\n"
					+"<location>"+ location +"</location>\n"
					+"<realname>"+ realname +"</realname>\n"
					+"<uname>"+ name + "_" + uid.toString() +"</uname>\n"
					+"</asset>\n";
				XhiveLibraryIf soLibrary = (XhiveLibraryIf)rootLibrary.get(metalibrary);
				Document doc = (Document)soLibrary.get("metadata-meta.xml");
//				System.out.println(doc.toString());
				IterableIterator result = soLibrary.executeXQuery(insqry, (XhiveDocumentIf)doc);
				// We know this query will only return a single node.
				XhiveXQueryValueIf value = (XhiveXQueryValueIf)result.next();
				XhiveNodeIf node = value.asNode();
				//node.set
				// Append it to the document element of destination document
				doc.getDocumentElement().appendChild(node);
				lib = (XhiveLibraryIf)rootLibrary.get(otherslibrary);
				if (!saveXMLDoc(name + "_" + uid.toString(), rootpath + "/" +metalist.get(i),lib))
				{
					System.out.println("SOM.saveSO:Error " + rootpath + "/" +metalist.get(i));
					return false;
				}
			}
			return true;

		}catch(Exception ex)
		{
			System.out.println("SOM.saveSO:Error - "+ex.getMessage() + "\n" + ex.getStackTrace().toString());
		}

		return false;
	}
	
	public boolean saveBlob(String name,String path)
	{
		//TODO:
		try
		{
			XhiveLibraryIf soLibrary = (XhiveLibraryIf)rootLibrary.get(assetlibrary);
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

	public boolean saveXMLDoc(String objectname,String path,XhiveLibraryIf manlibrary)
	{
		//TODO:
		try
		{
			//XhiveLibraryIf manlibrary = (XhiveLibraryIf)rootLibrary.get(manifestlibrary);
			//create a DOMBuilder
			LSParser builder = manlibrary.createLSParser();
			// parse a new document
			Document doc = builder.parseURI(new File(path).toURI().toString());
			String docname = objectname + "_" +path.substring(1+path.lastIndexOf('/'));//"_imsmanifest.xml";
			if (!(manlibrary.nameExists(docname))) 
			{
				// add the new document to the library
				manlibrary.appendChild(doc);
				((XhiveDocumentIf)doc).setName(docname);
			}
			else
			{
				System.out.println("Document Already Present");
				return false;
			}
			return true;

		}catch(Exception ex)
		{
			System.out.println("SOM.saveXMLDoc:Error - "+ex.getMessage());
		}

		return false;

	}
	
	public boolean retriveXMLDoc(String objectname,String fullpath,XhiveLibraryIf manlibrary)
	{
		//TODO:
		try
		{
			//create a DOMBuilder
			LSParser builder = manlibrary.createLSParser();
			// parse a new document
			String docname = objectname;
			if (!(manlibrary.nameExists(docname))) 
			{
				System.out.println("SOM.retriveXMLDoc: Error - Document Not Present Present");
				return false;
			}
			else
			{
				XhiveDocumentIf doc = (XhiveDocumentIf)manlibrary.get(docname);
				//doc.toString();
				String content = doc.toXml();
				FileOutputStream out;// = new FileOutputStream(path);
				try{
					out = new FileOutputStream(fullpath);
					}catch(FileNotFoundException fex)
					{
						new File(fullpath.substring(0, fullpath.lastIndexOf('/'))).mkdirs();
						
					}
					finally
					{
						out = new FileOutputStream(fullpath);
					}
				int sosize = (int)content.length();
				out.write(content.getBytes(), 0, sosize);
				
			}
			return true;

		}catch(Exception ex)
		{
			System.out.println("SOM.saveXMLDoc:Error - "+ex.getMessage());
		}

		return false;

	}

//	public boolean retriveSO(String name,String path)
//	{
//		try
//		{
//			XhiveLibraryIf soLibrary = (XhiveLibraryIf)rootLibrary.get(libraryName);
//			XhiveBlobNodeIf so =  (XhiveBlobNodeIf)soLibrary.get(name);
//			//			so.setName(name);
//			//			so.setContents(new FileInputStream(path));
//			//			soLibrary.appendChild(so);
//			InputStream in = so.getContents();
//			FileOutputStream out = new FileOutputStream(path);
//			int sosize = (int)so.getSize();
//			byte[] buffer = new byte[sosize];
//			int length;
//			while((length = in.read(buffer)) != -1) {
//				out.write(buffer, 0, length);
//			}
//
//			//System.out.println(so.getName());
//			return true;
//
//		}catch(Exception ex)
//		{
//			System.out.println("SOM.retriveSO:Error - "+ex.getMessage());
//		}
//
//		return false;
//	}
	public boolean retriveBlob(String name,String fullpath)
	{
		try
		{
			XhiveLibraryIf soLibrary = (XhiveLibraryIf)rootLibrary.get(assetlibrary);
			XhiveBlobNodeIf so =  (XhiveBlobNodeIf)soLibrary.get(name);
			//			so.setName(name);
			//			so.setContents(new FileInputStream(path));
			//			soLibrary.appendChild(so);
			InputStream in = so.getContents();
			FileOutputStream out;
			try{
			out = new FileOutputStream(fullpath);
			}catch(FileNotFoundException fex)
			{
				new File(fullpath.substring(0, fullpath.lastIndexOf('/'))).mkdirs();
				
			}
			finally
			{
				out = new FileOutputStream(fullpath);
			}
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
	public boolean retriveSO(String name,String path)
	{
		String rootpath = path;
		try
		{
			XhiveLibraryIf mlibrary = (XhiveLibraryIf)rootLibrary.get(metalibrary); 
			ArrayList<String> assetlist = getComponentsList(name, "asset");
			for(int i=0;i<assetlist.size();i++)
			{
				int temp = assetlist.get(i).lastIndexOf('/');
				String location = (assetlist.get(i)).substring(0, temp);
				String realname = (assetlist.get(i)).substring(temp+1);
				
				String theQuery = "declare namespace loc = 'http://www.iiitb.ac.in/docengg';\n" 
					+"for $i in doc('/som-metadata/asset-meta.xml')//loc:asset \n"
					+"where $i/loc:object='" + name + "' and $i/loc:location='"+ location +"' and $i/loc:realname='"+ realname +"' \n"
					+"return data($i/loc:uname)";
				//System.out.println(theQuery);
				IterableIterator<? extends XhiveXQueryValueIf> result = mlibrary.executeXQuery(theQuery);
				
				//int count = 0;
				// Process the results
				while (result.hasNext()) {
					// Get the next value from the result sequence
					XhiveXQueryValueIf value = result.next();
					//System.out.println(value.toString());
					if(!retriveBlob(value.toString(), rootpath + "/"+location+"/"+realname))
					{
						System.out.println("SOM.retriveSO:Error");
						return false;
					}
				}
			}
			mlibrary = (XhiveLibraryIf)rootLibrary.get(manifestlibrary);
			if(!retriveXMLDoc(name+"_imsmanifest.xml", rootpath+"/imsmanifest.xml", mlibrary))
			{
				System.out.println("SOM.retriveSO:Error");
				return false;
			}
			ArrayList<String> metalist = getComponentsList(name, "metadata");
			for(int i=0;i<metalist.size();i++)
			{
				int temp = metalist.get(i).lastIndexOf('/');
				String location = (metalist.get(i)).substring(0, temp);
				String realname = (metalist.get(i)).substring(temp+1);
				
				String theQuery = "declare namespace loc = 'http://www.iiitb.ac.in/docengg';\n" 
					+"for $i in doc('/som-metadata/metadata-meta.xml')//loc:asset \n"
					+"where $i/loc:object='" + name + "' and $i/loc:location='"+ location +"' and $i/loc:realname='"+ realname +"' \n"
					+"return data($i/loc:uname)";
				System.out.println(theQuery);
				IterableIterator<? extends XhiveXQueryValueIf> result = mlibrary.executeXQuery(theQuery);
				mlibrary = (XhiveLibraryIf)rootLibrary.get(otherslibrary);
				//int count = 0;
				// Process the results
				while (result.hasNext()) {
					// Get the next value from the result sequence
					XhiveXQueryValueIf value = result.next();
					//System.out.println(value.toString());
					if(!retriveXMLDoc(value.toString(), rootpath + "/"+location+"/"+realname,mlibrary))
					{
						System.out.println("SOM.retriveSO:Error");
						return false;
					}
				}
			}


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
	
	

	public ArrayList<String> getComponentsList(String objectname,String comptype)
	{
		ArrayList<String> ret = new ArrayList<String>();
	
		try
		{
			String docname = objectname+"_imsmanifest.xml";
			XhiveLibraryIf manlibrary = (XhiveLibraryIf)rootLibrary.get(manifestlibrary);
			
			// Create a query (find all the short chapter titles)
			String theQuery = "";
			if(comptype.equals("asset"))
			{
				theQuery = "declare namespace ims = 'http://www.imsproject.org/xsd/imscp_rootv1p1p2';\n" 
							+"declare namespace adlcp='http://www.adlnet.org/xsd/adlcp_rootv1p2';\n" 
							+"for $i in doc('/manifests/" + docname +"')//ims:file \n"
							+"return data($i/@href)";
			}
			else if(comptype.equals("metadata"))
			{
				theQuery = "declare namespace ims = 'http://www.imsproject.org/xsd/imscp_rootv1p1p2';\n" 
							+"declare namespace adlcp='http://www.adlnet.org/xsd/adlcp_rootv1p2';\n" 
							+"for $i in doc('/manifests/" + docname +"')//adlcp:location \n"
							+"return data($i)";
			}
			else
			{
				System.out.println("SOM.getComponentsList: Error - Invalid type");
				return ret;
			}
			

			// Execute the query (place the results in the new document)
			//System.out.println("#running query:\n" + theQuery);
			IterableIterator<? extends XhiveXQueryValueIf> result = manlibrary.executeXQuery(theQuery);
			
			//int count = 0;
			// Process the results
			while (result.hasNext()) {
				// Get the next value from the result sequence
				XhiveXQueryValueIf value = result.next();

				// Print this value
				ret.add(value.toString());
				//System.out.println(value.toString());
				//count++;
			}
			//return count;
		}catch(Exception ex)
		{
			System.out.println("SOM.getComponentList:Error - "+ex.getMessage());
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
