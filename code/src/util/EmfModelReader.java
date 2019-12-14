package util;
import interfaces.IModelReader;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;

public class EmfModelReader implements IModelReader<Resource, EPackage, EClass, EAssociation, EAttribute, EOperation> {
  Resource r;
  public EmfModelReader(Resource r) {
    this.r = r;
  }
  
  @Override
  public Resource getModelResource() {
    return r;
  }
  @Override
  public Resource getResource() {
	return r;
}
  @Override
  public List<EPackage> getPackages() {
          LinkedList<EPackage> pList = new LinkedList<EPackage>();
          if (r.getContents() != null)
                  for (EObject obj : r.getContents())
                          if (obj instanceof EPackage) {
                                  EPackage pack = (EPackage) obj;
                                  retrieveSubPackages(pack, pList);
                                  pList.add((EPackage) obj);
                                  EPackage.Registry.INSTANCE.put(((EPackage) obj).getNsURI(),
                                                  (EPackage) obj);
                          }
          return pList;
  }

  private void retrieveSubPackages(EPackage parentPackage,
                  List<EPackage> allPackages) {
          for (EPackage subPackage : parentPackage.getESubpackages()) {
                  allPackages.add(subPackage);
                  EPackage.Registry.INSTANCE.put(subPackage.getNsURI(), subPackage);
                  retrieveSubPackages(subPackage, allPackages);
          }
  }
	public static List<EClass> getClassesFromPackage(EPackage p) {
	  LinkedList<EClass> cList = new LinkedList<EClass>();
	  if (p.getEClassifiers() != null)
	    for (EClassifier c : p.getEClassifiers())
	      if (c instanceof EClass) 
	        cList.add((EClass)c);
	  return cList;
	}

	@Override
	public List<EClass> getClasses() {
	  List<EPackage> pList = getPackages();
	  LinkedList<EClass> cList = new LinkedList<EClass>();          
	  for(EPackage p : pList) 
	    cList.addAll(getClassesFromPackage(p));  
	  return cList;
	}
	
	public List<EEnum> getEnumerations() {
		  List<EPackage> pList = getPackages();
		  LinkedList<EEnum> cList = new LinkedList<EEnum>();          
		  for(EPackage p : pList) 
		    cList.addAll(getEnumerationsFromPackage(p));  
		  return cList;
		}
	
	
	
	private List<EEnum> getEnumerationsFromPackage(EPackage p) {
		  LinkedList<EEnum> cList = new LinkedList<EEnum>();
		  if (p.getEClassifiers() != null)
		    for (EClassifier c : p.getEClassifiers())
		      if (c instanceof EEnum) 
		        cList.add((EEnum)c);
		  return cList;
		}
	
	
	public List<EClass> getClasses(EPackage pck) {   
	    return getClassesFromPackage(pck);  
	  
	}

	
	@Override
	public List<String> getClassesNames() {
    List<EClass> cList = getClasses();
    LinkedList<String> names = new LinkedList<String>();
    for(EClass c : cList)
      names.add(c.getName());   
    return names;
	}

  @Override
  public List<EAttribute> getClassAttributes(EClass c) {
	  return c.getEAttributes();
  }

  @Override
  public List<EOperation> getClassOperations(EClass c) {
    LinkedList<EOperation> opList = new LinkedList<EOperation>();
    if (c.getEOperations() != null)
      for (EOperation op : c.getEOperations())
        opList.add(op);
    return opList; 
  }
  
  public List<EOperation> getAllOperations() {
	  LinkedList <EClass> allClasses =  (LinkedList<EClass>)getClasses();
	  LinkedList<EOperation> opList = new LinkedList<EOperation>();
	  for (EClass c:allClasses)
	    if (c.getEOperations() != null)
	      for (EOperation op : c.getEOperations())
	        opList.add(op);
	    return opList; 
	  }
  
  public List<EOperation> getAllOperations(EPackage pck) {
	  LinkedList <EClass> allClasses =  (LinkedList<EClass>)getClasses(pck);
	  LinkedList<EOperation> opList = new LinkedList<EOperation>();
	  for (EClass c:allClasses)
	    if (c.getEOperations() != null)
	      for (EOperation op : c.getEOperations())
	        opList.add(op);
	    return opList; 
	  }
  
  @Override
  public List<EClass> getClassSubtypes(List<EClass> classList, EClass c  ) {
    LinkedList<EClass> subTypesList = new LinkedList<EClass>();  
    if (classList != null) 
      for (EClass cl : classList) 
        for (EClass superType : cl.getESuperTypes()) 
          if (c.equals(superType))
            subTypesList.add(cl);
    return subTypesList.size() > 0 ? subTypesList : null;
  }
  public void getClassSubtypes(List<EClass> classList , EClass c , List<EClass> nestedSubtypes){
	  if (classList != null) 
	      for (EClass cl : classList) 
	        for (EClass superType : cl.getESuperTypes()) 
	          if (c == superType){
	        	  nestedSubtypes.add(cl);
	        	  getClassSubtypes(classList , cl ,  nestedSubtypes);}
	   
  }
  
  @Override
  public EClass getBaseClass(EClass c) {
    if (c.getESuperTypes() == null || (c.getESuperTypes() != null && c.getESuperTypes().size() == 0))
      return c;
    if (c.getESuperTypes().size() > 1)
      return null;
    return getBaseClass(c.getESuperTypes().get(0));
  }  
  
  
  public static EClass getBaseC(EClass c) {
	    if (c.getESuperTypes() == null || (c.getESuperTypes() != null && c.getESuperTypes().size() == 0))
	      return c;
	    if (c.getESuperTypes().size() > 1)
	      return null;
	    return getBaseC(c.getESuperTypes().get(0));
	  }  
  

  
	@Override
	public List<EAssociation> getAssociations() {

    
    LinkedList<EAssociation> asList = new LinkedList<EAssociation>();
    
    List<EClass> cList = getClasses();
    for (EClass c : cList) 
    { 
 
      if (c.getEReferences() != null) 
        for (EReference ref : c.getEReferences()) {
             EAssociation as = new EAssociation("", c, ref);
            asList.add(as);
          
        }
    }
    return asList;
	}
  
	@Override
	public List<String> getAssociationsNames() {
	  List<EAssociation> asList = getAssociations();
    LinkedList<String> names = new LinkedList<String>();
    for(EAssociation as : asList)
      names.add(as.getName());   
    return names;
	}
	@Override	
	public List<String> getAssociationNamesOfNonAbsClasses(){
		List<EAssociation> asList = getAssociations();
	    LinkedList<String> names = new LinkedList<String>();
	    for(EAssociation as : asList)
	    	if(!assWithAbsEnd(as))
	    		names.add(as.getName());
	    return names;
	}
	private boolean assWithAbsEnd(EAssociation as){
		
		EClass srcCls = as.getSourceEnd();
		EClass trgCls = as.getDestinationEnd().getEReferenceType();
		return (srcCls.isAbstract() && getClassSubtypes(getClasses(), srcCls)==null) || 
				(trgCls.isAbstract() && getClassSubtypes(getClasses(), trgCls)==null);
	}
  @Override
  public String getAssociationName(EAssociation as) {
    return as.getName();
  }
  
  
  public EClass getClassByName( EPackage pck, String ch)
  {
	  EClass res=null;
	  int i=0;
	  boolean find = false;
	  List<EClass> liste = getClassesFromPackage(pck);
	  while(i<liste.size()&&find==false)
	  {
		  if(liste.get(i).getName().trim().equals(ch.trim()))
		  {
			  find=true;
			  res=liste.get(i);
		  }
		  else i++;
	  }
	  return res;
  }


  @Override
  public String getAssociationEndName(EAttribute asEnd) {
    return asEnd.getName();
  }
}