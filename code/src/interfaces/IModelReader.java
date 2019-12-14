
package interfaces;

import java.util.List;



public interface IModelReader<R, P, C, AS, AT, OP> {

	public R getModelResource(); 

	public List<P> getPackages();

	public List<C> getClasses();

	public List<String> getClassesNames();

	public List<AT> getClassAttributes(C c);

	public List<OP> getClassOperations(C c);  

	public List<C> getClassSubtypes(List<C> classList, C c);
	
	public void getClassSubtypes(List<C> cList, C c, List<C> subTypes);

	public C getBaseClass(C c);  

	public List<AS> getAssociations();

	public List<String> getAssociationsNames();

	public String getAssociationName(AS as);

	public String getAssociationEndName(AT asEnd);

	public List<String> getAssociationNamesOfNonAbsClasses();

	public R getResource();
}
