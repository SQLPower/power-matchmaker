package ca.sqlpower.matchmaker.munge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.matchmaker.AbstractMatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonProperty;
import ca.sqlpower.object.annotation.Transient;
import ca.sqlpower.object.annotation.ConstructorParameter.ParameterType;

/**
 * This is the pairing between a MungeStepOutput value and its InputDescriptor.
 * The reason for this class is to avoid maintaining two separate collections of
 * MungeStepOutputs and their InputDescriptors.
 * <p>
 * Note this class is only public because the XML DAO needs access to it. For
 * normal use of the MatchMaker API, there is no need to refer to this class
 * directly.
 */
public class MungeStepInput extends AbstractMatchMakerObject {
	
	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(InputDescriptor.class)));
	
	/**
	 * used by hibernate
	 */
	@SuppressWarnings("unused")
	private Long oid;
	
	/**
	 * TODO This is the same as the parent variable and should be removed.
	 */
	private MungeStep parentStep;
	
	/**
	 * The MungeStepOutput containing the value of this input.
	 */
	private MungeStepOutput current;
	
	/**
	 * The attributes of this input.
	 */
	private final InputDescriptor descriptor;
	
	/**
	 * only used by hibernate
	 */
	@SuppressWarnings("unused")
	private MungeStepInput() {
		descriptor = new InputDescriptor(null, null);
		descriptor.setParent(this);
	}
	
	public void disconnect() {
		MungeStepOutput old = current;
		current = null;
		firePropertyChange("current",old,null);
	}
	
	@Constructor
	public MungeStepInput(@ConstructorParameter(propertyName="current") MungeStepOutput current,
			@ConstructorParameter(parameterType=ParameterType.CHILD, propertyName="descriptor") InputDescriptor descriptor, 
			@ConstructorParameter(propertyName="parent") MungeStep step) {
		this.current = current;
		this.descriptor = descriptor;
		descriptor.setParent(this);
		this.parentStep = step;
		setParent(step);
	}

	/**
	 * only used by hibernate
	 */
	@SuppressWarnings("unused")
	private MungeStep getParentStep() {
		return parentStep;
	}

	/**
	 * only used by hibernate
	 */
	@SuppressWarnings("unused")
	private void setParentStep(AbstractMungeStep parentStep) {
		this.parentStep = parentStep;
	}

	@Accessor
    public MungeStepOutput getCurrent() {
		return current;
	}

	@Mutator
    public void setCurrent(MungeStepOutput current) {
    	MungeStepOutput former = this.current;
		this.current = current;
		firePropertyChange("current", former, current);
	}
    
    @Accessor
    public String getName() {
		return descriptor.getName();
	}
    
    @Mutator
    public void setName(String name) {
    	descriptor.setName(name);
    }
    
    @NonProperty
    public InputDescriptor getDescriptor() {
    	return descriptor;
    }

	/**
     * Returns the data type expected by this input.
     */
    @Transient @Accessor
    public Class<?> getType() {
		return descriptor.getType();
	}
    
	@Override
	public MatchMakerObject duplicate(MatchMakerObject parent) {
		return null;
	}

	@Override
	@NonProperty
	public List<Class<? extends SPObject>> getAllowedChildTypes() {
		return allowedChildTypes;
	}

	@Override
	@NonProperty
	public List<? extends SPObject> getChildren() {
		return Collections.singletonList(descriptor);
	}
}