package bwfdm.sara.publication;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Hierarchy {
	@JsonProperty
	private String name = null;
	@JsonProperty
	private String policy = null;
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

	public Hierarchy(final String name, final String policy) {
		this.name = name;
		this.policy = policy;
	}

	public void addChild(final Hierarchy child) {
		child.setParent(this);
		this.children.add(child);
	}

	public Hierarchy addChild(final String name, final String policy) {
		Hierarchy newChild = new Hierarchy(name, policy);
		newChild.setParent(this);
		children.add(newChild);
		return newChild;
	}

	public void addChildren(final List<Hierarchy> children) {
		for (Hierarchy t : children) {
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

	public void setCollection(final boolean coll) {
		is_collection = coll;
	}

	public String getName() {
		return name;
	}

	public String getPolicy() {
		return policy;
	}

	public String getHandle() {
		return handle;
	}

	public String getURL() {
		return URL;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setPolicy(final String policy) {
		this.policy = policy;
	}

	public void setHandle(final String handle) {
		this.handle = handle;
	}

	public void setURL(final String URL) {
		this.URL = URL;
	}

	private void setParent(final Hierarchy parent) {
		this.parent = parent;
	}

	public Hierarchy getParent() {
		return parent;
	}

	public void dump(final String path) {
		for (Hierarchy c : getChildren()) {
			c.dump(path + ">" + c.name);
		}

		if (getChildrenCount() == 0) {
			System.out.print(path + " ");
		}
	}

	public int getCollectionCount() {
		if (is_collection) {
			return 1;
		} else {
			int sum = 0;
			for (Hierarchy child : getChildren()) {
				sum = sum + child.getCollectionCount();
			}
			return sum;
		}
	}

	private int submittableSum(final Hierarchy h) {
		int partSum = h.is_collection ? (1) : (0);
		for (final Hierarchy ch : new ArrayList<Hierarchy>(h.children)) {
			final int partSum2 = submittableSum(ch);
			if (partSum2 == 0) {
				h.children.remove(ch);
			}
			partSum = partSum + partSum2;
		}
		return partSum;
	}

	public void pruneUnsubmittable() {
		int sum = submittableSum(this);
		System.out.println(
				"Pruned all unsubmittable entries, total left: " + sum);
	}
}
