package util;

import java.util.Collection;

import org.eclipse.emf.ecore.EObject;

public class Container {
	
	public String itteratorName;
	public EObject itterator;
	public String collectionName;
	public Collection<EObject> collection;
	

	

	public Container(String itteratorName, EObject itterator, String collectionName, Collection<EObject> collection) {
		super();
		this.itteratorName = itteratorName;
		this.itterator = itterator;
		this.collectionName = collectionName;
		this.collection = collection;
	}

	public String getItteratorName() {
		return itteratorName;
	}

	public void setItteratorName(String itteratorName) {
		this.itteratorName = itteratorName;
	}

	public EObject getItterator() {
		return itterator;
	}

	public void setItterator(EObject itterator) {
		this.itterator = itterator;
	}

	public Collection<EObject> getCollection() {
		return collection;
	}

	public void setCollection(Collection<EObject> collection) {
		this.collection = collection;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}



	@Override
	public String toString() {
		return "Container [itteratorName=" + itteratorName + ", itterator=" + itterator + ", collectionName="
				+ collectionName + ", collection=" + collection + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((collection == null) ? 0 : collection.hashCode());
		result = prime * result + ((collectionName == null) ? 0 : collectionName.hashCode());
		result = prime * result + ((itterator == null) ? 0 : itterator.hashCode());
		result = prime * result + ((itteratorName == null) ? 0 : itteratorName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Container other = (Container) obj;
		if (collection == null) {
			if (other.collection != null)
				return false;
		} else if (!collection.equals(other.collection))
			return false;
		if (collectionName == null) {
			if (other.collectionName != null)
				return false;
		} else if (!collectionName.equals(other.collectionName))
			return false;
		if (itterator == null) {
			if (other.itterator != null)
				return false;
		} else if (!itterator.equals(other.itterator))
			return false;
		if (itteratorName == null) {
			if (other.itteratorName != null)
				return false;
		} else if (!itteratorName.equals(other.itteratorName))
			return false;
		return true;
	}




	
}
