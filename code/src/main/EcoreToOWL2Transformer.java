package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EEnumImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.eclipse.uml2.uml.resources.util.UMLResourcesUtil;

import util.EAssociation;
import util.EmfModelReader;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;




@SuppressWarnings("deprecation")
public class EcoreToOWL2Transformer {

	static Resource resource=null;
	private static String toWrite;
	private static ArrayList<EReference> visited;
	
	private static Date d;

	public static void main(String[] args) throws Exception {
		d = new Date();
		String defaultFile ="./Models/FlurstueckeUML22.ecore";
		
		if(args.length>0){
			defaultFile = args[0];
		}
		
		File tmpDir = new File(defaultFile);
		
		if(!tmpDir.exists()){
			System.err.println("File "+defaultFile+" does not exist or not accessable.");
			System.exit(0);
		}
		
		
		convertToTurtle(defaultFile);
		//final String absolutePath = args[0];
		String ext = FilenameUtils.getExtension(defaultFile); 

		if (ext.equalsIgnoreCase("ecore"))
		{
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
					"ecore", new EcoreResourceFactoryImpl());
		}
		//doesnt work... :(
		else if (ext.equalsIgnoreCase("uml")) 	{
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
					"uml", new UMLResourceFactoryImpl());

		}else if (ext.equalsIgnoreCase("xml")) 	{
			System.out.println("XML File cannot converted to OWL");
			System.exit(0);
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
					"xml", new UMLResourceFactoryImpl());

		} else{System.out.println("Unsupported extension");
		System.exit(0);
		}

		ResourceSet rs = new ResourceSetImpl(); 
		final ExtendedMetaData extendedMetaData = new BasicExtendedMetaData(rs.getPackageRegistry());
		rs.getLoadOptions().put(XMLResource.OPTION_EXTENDED_META_DATA,extendedMetaData);

		// creating the URI and getting the resources from the .ecore file
		URI modelFileURI = URI.createFileURI(defaultFile);          
		resource=null;
		if (ext.equalsIgnoreCase("ecore"))
			resource = loadEmfResource(modelFileURI,rs);

		Iterator<EObject> i = resource.getAllContents();
		while (i.hasNext()) {
			Object o = i.next();
			if (o instanceof EPackage) {
				EPackage p = (EPackage)o;
				rs.getPackageRegistry().put(p.getNsURI(), p);       	

			}
		}
		// Rule 11: Primitive data types -> goal datatype from Ecore Schema and not XML (Both are similar)
		//writting the prefix of the ontology document
		visited= new ArrayList<EReference>();
		toWrite="";
		toWrite="Prefix(:=<http://example.com/owl/UMLClassDiagram/>)\n";
		toWrite+="Prefix(xsd:<=http://www.w3.org/2001/XMLSchema#>)\n";
		toWrite+="Prefix(owl:<=http://www.w3.org/2002/owl2#>)\n";
		toWrite+="Ontology(<=http://example.com/owl/UMLClassDiagram>)\n";
		//read the model
		//Getting all the data to be transform from the .ecore file (Classes , Subclasses, Properties, etc
		EmfModelReader reader = new EmfModelReader(resource);     

		EPackage pack = reader.getPackages().get(0);


		LinkedList<EClass> allClasses = new LinkedList<EClass>();
		LinkedList<EEnum> allEnumerations = new LinkedList<EEnum>();
		allClasses.addAll(reader.getClasses());
		allEnumerations.addAll(allEnumerations);
		writeOWL(pack);
		toWrite="";
		writeTurtle();
	}
	
	private static void convertToTurtle(String ecore) throws ClientProtocolException, IOException{
		String mainFile = ecore;
		
		String content = "";
	    try
	    {
	        content = new String ( Files.readAllBytes( Paths.get(mainFile) ) );
	    }
	    catch (IOException e)
	    {
	        e.printStackTrace();
	    }
		
		String res = Request.Post("http://www.easyrdf.org/converter")
        .bodyForm(Form.form().add("data", content).add("raw", "1").add("uri","http://njh.me/").add("in","rdfxml").add("out", "turtle").build())
        .execute()
        .returnContent().asString();
		
		if(res.contains("<div class=\"alert alert-block span9\">")){
			int index = res.indexOf("<div class=\"alert alert-block span9\">");
			index += "<div class=\"alert alert-block span9\">".length();
			int limit = res.indexOf("</div>",index);
			String msg = res.substring(index, limit);
			System.err.println(msg);
			return;
		}
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(d.getTime()+"turtleConverted.ttl")));

		bw.write(res);
		bw.close();
		System.out.println("Turtle Converted from XML @ :" +mainFile);
		
	}// convertToTurtle


	private static void writeTurtle() throws IOException {



		visited= new ArrayList<EReference>();
		toWrite="";
		toWrite="Prefix(:=<http://example.com/owl/UMLClassDiagram/>)\n";
		toWrite+="Prefix(xsd:<=http://www.w3.org/2001/XMLSchema#>)\n";
		toWrite+="Prefix(owl:<=http://www.w3.org/2002/owl2#>)\n";
		toWrite+="Ontology(<=http://example.com/owl/UMLClassDiagram>)\n";
		//read the model
		EmfModelReader reader = new EmfModelReader(resource);     

		EPackage pack = reader.getPackages().get(0);


		LinkedList<EClass> allClasses = new LinkedList<EClass>();
		LinkedList<EEnum> allEnumerations = new LinkedList<EEnum>();
		allClasses.addAll(reader.getClasses());
		allEnumerations.addAll(allEnumerations);

		EList<EClassifier> classifiers = pack.getEClassifiers();
		System.out.println("Transformation start");

		for(EClassifier ec:classifiers)
		{
			handleClassifierTurtle(ec);
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(d.getTime()+"owl2.turtle")));
		String[] pts = toWrite.split("\n");

		bw.write(toWrite+"\n");
		bw.close();
		System.out.println("Transformation Complete\n\nTransformed model is saved in owl2.turtle file");



	}

	private static void handleClassifierTurtle(EClassifier ec) {
		// TODO Auto-generated method stub


		if(ec instanceof EEnumImpl)
		{

			EEnum en=(EEnum)ec;
			EList<EEnumLiteral> literals = en.getELiterals();
			toWrite+=":"+en.getName()+"owl:equivalentClass [ rdf:type owl:Enumeration \n";
			toWrite+="DataOneOf(";
			for(EEnumLiteral eel:literals)
			{
				toWrite+="\""+eel.getName()+"\"^^xsd:String ";
			}
			toWrite+="\t\t\n";


		}
		else
		{
			toWrite+="\n:"+ec.getName()+"rdf:type owl:Class ;\n\nowl:equivalentClass [ rdf:type owl:Class ;\n\n";
			//System.out.println(ec.getName());
			EList<EObject> contents = ec.eContents();
			//System.out.println(contents);


			EObject cons = contents.get(0);

			for (EObject eob:contents)
			{
				if(eob instanceof EAttribute)
				{
					EAttribute at=(EAttribute) eob;
					//System.err.println(at);
					{
						toWrite+="\t\towl:onProperty :"+at.getName()+";\n";

						toWrite+=" \t\t\towl:someValuesFrom [ rdf:type rdfs:Datatype ;\n";

						if(at.getEType()!=null)
						{
							toWrite+="\t\t\t\towl:onDatatype xsd: "+
									at.getEType().getName()+
									";\n";
						}
						else
						{
							toWrite+="\t\t\t\towl:onDatatype xsd: "+
									"String"+
									":\n";
						}

						toWrite+="\t\t\towl:withRestrictions ( "+

					"\t\t\t[ xsd:maxExclusive \""+at.getUpperBound()+"\"^^xsd:integer ]"+
					"\t\t\t[ xsd:minExclusive \""+at.getLowerBound()+"\"^^xsd:integer]"
					+"";

					}
					//
					//				
				}
				else if(eob instanceof EReference)
				{}
			}

			toWrite+="]\n]\n)\n]\n\n";


		}
	}
	private static void writeOWL(EPackage pack) throws IOException {       
		//Start Transformation
		EList<EClassifier> classifiers = pack.getEClassifiers();
		System.out.println("Transformation start");
		//Loop to call and execute the function handleClassifier and go throw all the data of from .ecore file already saved in EClasifier 
		//transforming it in to owl schema and print it in the new document "owl2.owl" 
		for(EClassifier ec:classifiers)
		{
			handleClassifier(ec);
		}

		handleRule14n15();
		handleRule16();
		//Destination file .owl
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(d.getTime()+"owl2.owl")));
		String[] pts = toWrite.split("\n");

		bw.write(toWrite+"\n");
		bw.close();
		System.out.println("Transformation Complete\n\nTransformed model is saved in owl2.owl file");

	}
	
	
	private static void handleRule16() {
		Set<String> set = hashMap.keySet();
		String toWr="";
		ArrayList<String> eclses=new ArrayList<>();
		for(String s:set)
		{
			EList<EClass> v = hashMap.get(s);
			for(EClass ec:v)
			{
				eclses.add(ec.getName());
			}
		}

		for(String s:eclses)
		{
			boolean b=false;
			String r="";
			r="IntersectionOf(";
			if(set.contains(s))
			{
				r+=":"+s;
				b=true;
			}
			if(b)
			{
				toWrite+=r+"\nSubClassOf( :"+s+"\n"+"))";
				
			}
		}

	}

    //Name say already the rules implemented here :)
	private static void handleRule14n15() {
		Set<String> set = hashMap.keySet();
		String toWr="";
		for(String s:set)
		{
			toWr+=":"+s;
			toWrite+="EquivalentClasses(:"+s+"\nObjectUnionOf(";
			EList<EClass> v = hashMap.get(s);
			for(EClass ec:v)
			{
				toWrite+=" :"+ec.getName();
				toWr+=":"+ec.getName();
			}
			toWr+=")";

			toWrite+=")\n)";
			toWrite+=toWr;
		}


	}
	static HashMap<String, EList> hashMap=new HashMap<>();
	//Beginning of the process
	private static void handleClassifier(EClassifier ec) {

		if(ec instanceof EEnumImpl)
		{
			//Rule 18: transformation of generalization between datatypes => "DatatypeDefintion" & "DataUnionOf" axiom.
			//Rule 19: Data range "DataOneOf" axiom defining an equivalent datatype to enumeration.
			EEnum en=(EEnum)ec;
			EList<EEnumLiteral> literals = en.getELiterals();
			toWrite+="\nDatatypeDefinition(\n:"+en.getName()+")\n";
			toWrite+="DataOneOf(";
			for(EEnumLiteral eel:literals)
			{
				toWrite+="\""+eel.getName()+"\"^^xsd:String ";
			}
			toWrite+="))";

		}
		
		if(ec instanceof EClass)
		{
			EClass ecc=(EClass) ec;

			EList<EClass> st = ecc.getESuperTypes();
			hashMap.put(ecc.getName(), st);

		}
		else
		{
			//Rule 2: Classes to represent concepts of a domain. 
			// If a class was found introduce it and start to do loops to introduce the content of it depending of its properties.
			//Rule 1 too.
			toWrite+="\nDeclaration(Class(:"+ec.getName()+"))\n"; 
			//System.out.println(ec.getName());
			EList<EObject> contents = ec.eContents();
			//System.out.println(contents);



			EObject cons = contents.get(0);

			for (EObject eob:contents)
			{

				//It have a Attributes? - In OWL Properties?
				if(eob instanceof EAttribute)
				{
					EAttribute at=(EAttribute) eob;
					//System.out.println(at);
					{
						//Rule 4: Every simple association in UML is converted into an ObjectProperty axiom in OWL2. = "InverseObjectProperties" axiom
						toWrite+="Declaration(DataProperty(:"+at.getName()+"))\n"; //====Rule 4

						toWrite+="DataPropertyDomain(:"+at.getName()+":"+ec.getName()+")\n";

						if(at.getEType()!=null)
						{
							toWrite+="DataPropertyRange(:"+at.getName()+":"+
									at.getEType().getName()+
									")\n";
						}
						else
						{
							toWrite+="DataPropertyRange(:"+at.getName()+":"+
									"xsd:String"+
									")\n";
						}
						//Rule 10: idAttr implies that the values of the data type property that represent this attribute must be unique. 
						//These properties must be declared with HasKey properties.
						if(at.isID())
						{
							toWrite+="HasKey(:"+ec.getName()+"()(:"+at.getName()+"))\n";
						}

					}
				}
				/**
				 *  Are two elements connected via an instance? => Generalization in UML
				 *  For OWLis created the instance "SubClassOf" meta-class
				 *  Rule 12: UML "Generalization" = OWL "SubClassOf"
				 *  Rule 13: generalization-Specialization between classes by adding a DisjointClasses
				 */
				else if(eob instanceof EGenericType)
				{
					EGenericType et=(EGenericType)eob;
					toWrite+="SubClassOf(:"+ec.getName()+":"+
							et.getEClassifier().getName()+
							")\n";
					toWrite+="DisjointClasses(:"+ec.getName()+":"+
							et.getEClassifier().getName()+
							")\n";
				}
				// Rule 17:
				else if(eob instanceof EAssociation)
				{
					EAssociation et=(EAssociation)eob;
					toWrite+="SubPropertyOf(:"+ec.getName()+":"+
							et.getDestinationEnd().getName()+
							")\n";
					
				}


				else if(eob instanceof EReference)
				{

					EReference er=(EReference)eob;
					if(!visited.contains(er))
					{
						EReference oppo = er.getEOpposite();
						if(oppo==null)
						{
							continue;
						}
						//Rule 8: reflexive association in UML is converted into OWL2 by using the "ReflexiveObjectProperty" axiom.
						if(er.getEOpposite().getContainerClass().getName() == er.getContainerClass().getName())
						{
							toWrite="\nReflexiveObjectProperty( :"+er.getName()+" )\n";
							toWrite="\nReflexiveObjectProperty( :"+er.getEOpposite().getName()+" )\n";
						}
						if(er.getUpperBound()==1 &&er.getLowerBound()==1)	//Rule 9: Transformation of multiplicity constraints
						{
							toWrite="\nObjectExactCardinality(1 :"+er.getName()+" )\n";
						}

						if(er.getUpperBound()==1 &&er.getLowerBound()==0)	//Rule 9: Transformation of multiplicity constraints
						{
							toWrite="\nObjectMaxCardinality(1 :"+er.getName()+" )\n";
						}
						if(er.getUpperBound()==-1 &&er.getLowerBound()==1)	//Rule 9: Transformation of multiplicity constraints
						{
							toWrite="\nObjectMinCardinality(1 :"+er.getName()+" )\n";
						}
						if((er.getName()==null && oppo.getName()!=null)||(er.getName() != null && oppo.getName()==null))
						{
							String name="";
							EReference err;
							if(er.getName()!=null)
							{
								err=er;
								name=er.getName();
							}
						
							/**
							 * Rule 4: Associations in UML (Ecore) is converted into an ObjectProperty and InverseObjectProperties in OWL2.
							 * Rule 2&3 : implementation DataPropertyDomain and DataPropertyRange - not only here
							 */
							else
							{name=oppo.getName();err=oppo;}
							/**
							 * Rule 5
							 * A direct mapping of a composition is not 
							 * feasible in OWL2 ontology. However, the part of
							 * the whole can be transformed like other simple
							 * associations into an "ObjectProperty" axiom by
							 * taking into account the following restrictions:
							 * The composition association is antisymmetric.
							 * We can transform this constraint by adding an
							 * "AsymmetricObjectProperty" axiom.
							 * The composition association is irreflexive (a
							 * class must not be in a composition relation to
							 * itself). For this restriction we can use the
							 * "IrreflexiveObjectProperty" axiom.
							 * An object of a class must not be part of more
							 * than one composition. We can achieve this
							 * restriction by adding
							 * "InverseFunctionalObjectProperty" axiom.
							 */
							if(er.isContainment()==true)
							{
								toWrite += "AsymmetricObjectProperty(:"+err.getEContainingClass().getName()+")";
							}
							toWrite+="Declaration(ObjectProperty(:"+name+":"+
									"))\n";							                   
							toWrite+="ObjectPropertyDomain(:"+name+":"+err.getEContainingClass().getName()+")\n";
							toWrite+="ObjectPropertyRange(:"+name+":"+err.getEContainingClass().getName()+")\n";
						}

						/**
						 * Rule 4: Associations in UML (Ecore) is converted into an ObjectProperty and InverseObjectProperties in OWL2
						 * Rule 6: class association with attributes is transformed to:
						 *     an OWL class, with data property for every additional attribute and 
						 *     two pairs of inverse object properties for every class connected to the association class
						 *     and object property chains between the different classes connected to the association class
						 * Rule 2&3 : implementation DataPropertyDomain and DataPropertyRange - not only here
						 *    For the Simple Attributes we create a data type property with its domain and range
						 *    the XSD type corresponding to the type of the attribute in the UML class diagram is integrated to -see below to see
						 *    how different xsds anre integrated depending of the type found in the .ecore file.
						 * Rule 7: complex datatype is "ObjectProperty" axiom => unidirectional ObjectProperty (ObjectPropertyRange( :Person_Nom :Name ))
						 */

						else
						{
							String name=ec.getName();
							toWrite+="Declaration(ObjectProperty(:"+name+":"+
									"))\n";
							toWrite+="ObjectPropertyDomain(:"+name+":"+er.getName()+")\n";
							toWrite+="ObjectPropertyRange(:"+name+":"+oppo.getName()+")\n";

							toWrite+="InverseObjectProperties(:"+er.getName()+":"+oppo.getName()+")\n";
						}
						visited.add(er);
						//						visited.add(oppo);
					}

				}
			}


		}
		//		

		//	System.out.println(myClass.getEReferences());

	}

	//RULE 3
	private static String getPropertyType(String name, EAttribute at) {
		if(name.equalsIgnoreCase("estring"))
		{
			return "xsd:string";
		}
		if(name.equalsIgnoreCase("einteger"))
		{
			return "xsd:Integer";
		}
		if(name.equalsIgnoreCase("EInt"))
		{
			return "xsd:Integer";
		}
		if(name.equalsIgnoreCase("eboolean"))
		{
			return "xsd:Boolean";
		}
		if(name.equalsIgnoreCase("EEnumerator"))
		{
			//			System.out.println(at);
			//System.out.println(at.getEAttributeType());
			return "xsd:Enum";
		}
		if(name.equalsIgnoreCase("EJavaObject"))
		{
			//System.out.println(at);
			return"";
		}
		System.err.println("check "+name);
		return name;
	}
	//TODO update this to go into tuples rep


	public static Resource loadEmfResource(URI modelFileURI,ResourceSet rSet) {
		return  rSet.getResource(modelFileURI, true);
	}

}