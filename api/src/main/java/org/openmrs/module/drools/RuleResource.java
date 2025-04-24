package org.openmrs.module.drools;

import org.kie.api.io.ResourceType;

public class RuleResource {

	private String name;

	private String path;

	private ResourceType resourceType;

	public RuleResource() {

	}

	public RuleResource(String name, String path, ResourceType resourceType) {
		this.name = name;
		this.path = path;
		this.resourceType = resourceType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public ResourceType getResourceType() {
		return resourceType;
	}

	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}

	@Override
	public String toString() {
		return "RuleResource{" + "name='" + name + '\'' + ", path='" + path + '\'' + ", resourceType=" + resourceType
				+ '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof RuleResource))
			return false;
		RuleResource that = (RuleResource) o;
		return name.equals(that.name) && path.equals(that.path) && resourceType == that.resourceType;
	}
}
