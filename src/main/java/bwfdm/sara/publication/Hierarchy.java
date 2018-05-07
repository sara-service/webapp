package bwfdm.sara.publication;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Hierarchy{
	@JsonProperty
    private String name = null;
	@JsonProperty
	private String handle = null;
	@JsonProperty
	private String URL = null;
	@JsonProperty
	private boolean is_collection = false;
	@JsonManagedReference
    private List<Hierarchy> children = new ArrayList<>();
	@JsonBackReference
	private Hierarchy parent = null;

    public Hierarchy(String name) {
        this.name = name;
    }

    public void addChild(Hierarchy child) {
        child.setParent(this);
        this.children.add(child);
    }

    public Hierarchy addChild(String name) {
        Hierarchy newChild = new Hierarchy(name);
        newChild.setParent(this);
        children.add(newChild);
        return newChild;
    }

    public void addChildren(List<Hierarchy> children) {
        for(Hierarchy t : children) {
            t.setParent(this);
        }
        this.children.addAll(children);
    }

    public List<Hierarchy> getChildren() {
        return children;
    }
    
    public int getChildrenCount() {
    	return children.size();
    }
    
    public boolean isCollection() {
    	return is_collection;
    }
    
    public void setCollection(boolean coll) {
    	is_collection = coll;
    }

    public String getName() {
        return name;
    }
    
    public String getHandle() {
    	return handle;
    }
    
    public String getURL() {
    	return URL;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setHandle(String handle) {
    	this.handle = handle;
    }
    
    public void setURL(String URL) {
    	this.URL = URL;
    }

    private void setParent(Hierarchy parent) {
        this.parent = parent;
    }

    public Hierarchy getParent() {
        return parent;
    }
    
    public void dump(String path) {
		for (Hierarchy c: getChildren()) {
			c.dump(path + ">" + c.name);
		}

		if (getChildrenCount() == 0) {
			System.out.print(path + " ");
		}
    }
}
