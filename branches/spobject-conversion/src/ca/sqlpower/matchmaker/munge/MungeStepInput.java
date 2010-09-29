package ca.sqlpower.matchmaker.munge;

import java.util.Collections;
import java.util.List;

import ca.sqlpower.matchmaker.AbstractMatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonProperty;

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
	
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.emptyList();
	
	/**
	 * used by hibernate
	 */
	@SuppressWarnings("unused")
	private Long oid;
	
	private AbstractMungeStep parentStep;
	
	/**
	 * The MungeStepOutput containing the value of this input.
	 */
	private MungeStepOutput current;
	
	/**
	 * The attributes of this input.
	 */
	private InputDescriptor descriptor;
	
	/**
	 * only used by hibernate
	 */
	@SuppressWarnings("unused")
	private MungeStepInput() {
		descriptor = new InputDescriptor(null, null);
	}
	
	public void disconnect() {
		MungeStepOutput old = current;
		current = null;
		firePropertyChange("current",old,null);
	}
	
	@Constructor
	public MungeStepInput(@ConstructorParameter(propertyName="current") MungeStepOutput current,
			@ConstructorParameter(propertyName="descriptor") InputDescriptor descriptor, 
			@ConstructorParameter(propertyName="parent") AbstractMungeStep step) {
		this.current = current;
		this.descriptor = descriptor;
		this.parentStep = step;
		setParent(step);
	}

	/**
	 * only used by hibernate
	 */
	@SuppressWarnings("unused")
	private AbstractMungeStep getParentStep() {
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
    
    @Accessor
    public InputDescriptor getDescriptor() {
    	return descriptor;
    }

    @Mutator
    public void setDescriptor(InputDescriptor id) {
    	this.descriptor = id;
    }
    
	/**
     * Returns the data type expected by this input.
     */
    @Accessor
    public Class<?> getType() {
		return descriptor.getType();
	}
    
    @Mutator
    public void setType(Class<?> type) {
		descriptor.setType(type);
	}

	@Override
	public MatchMakerObject duplicate(MatchMakerObject parent,
			MatchMakerSession session) {
		return null;
	}

	@Override
	@NonProperty
	public List<Class<? extends SPObject>> getAllowedChildTypes() {
		return Collections.emptyList();
	}

	@Override
	@NonProperty
	public List<? extends SPObject> getChildren() {
		return Collections.emptyList();
	}
}