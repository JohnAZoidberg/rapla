package org.rapla.facade.internal;

import org.rapla.components.util.TimeInterval;
import org.rapla.entities.Entity;
import org.rapla.entities.RaplaObject;
import org.rapla.entities.RaplaType;
import org.rapla.entities.storage.EntityReferencer;
import org.rapla.facade.ModificationEvent;
import org.rapla.storage.UpdateOperation;
import org.rapla.storage.UpdateResult;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class ModificationEventImpl implements ModificationEvent
{
    private TimeInterval timeInterval;
    private boolean switchTemplateMode = false;
    private final Set<EntityReferencer.ReferenceInfo> removedReferences = new LinkedHashSet<EntityReferencer.ReferenceInfo>();
    private final Set<Entity> added = new LinkedHashSet<Entity>();
    private final Set<Entity> changed = new LinkedHashSet<Entity>();
    private final Set<RaplaType> modified = new LinkedHashSet<RaplaType>();
    public ModificationEventImpl()
    {

    }

    public ModificationEventImpl(UpdateResult updateResult)
    {
        final Iterable<UpdateOperation> operations = updateResult.getOperations();
        for (UpdateOperation op : operations)
        {
            final RaplaType raplaType = op.getRaplaType();
            modified.add(raplaType);
            if(op instanceof UpdateResult.Remove)
            {
                final Class<Entity> typeClass = raplaType.getTypeClass();
                removedReferences.add(new EntityReferencer.ReferenceInfo(op.getCurrentId(), typeClass));
            }
            else if(op instanceof UpdateResult.Change)
            {
                changed.add(updateResult.getLastKnown(op.getCurrentId()));
            }
            else if(op instanceof UpdateResult.Add)
            {
                added.add(updateResult.getLastKnown(op.getCurrentId()));
            }
        }
    }

    public boolean hasChanged(Entity object) {
        return getChanged().contains(object);
    }

    public boolean isRemoved(Entity object) {
        final EntityReferencer.ReferenceInfo referenceInfo = new EntityReferencer.ReferenceInfo(object);
        return getRemovedReferences().contains( referenceInfo);
    }

    public boolean isModified(Entity object)
    {
        return hasChanged(object) || isRemoved( object);
    }

    /** returns the modified objects from a given set.
     * @deprecated use the retainObjects instead in combination with getChanged*/
    public <T extends RaplaObject> Set<T> getChanged(Collection<T> col) {
        return RaplaType.retainObjects(getChanged(),col);
    }

    //    /** returns the modified objects from a given set.
    //     * @deprecated use the retainObjects instead in combination with getChanged*/
    //    public <T extends RaplaObject> Set<T> getRemoved(Collection<T> col) {
    //        return RaplaType.retainObjects(getRemoved(),col);
    //    }

    public Set<Entity> getChanged() {
        Set<Entity> result  = new HashSet<Entity>(getAddObjects());
        result.addAll(getChangeObjects());
        return result;
    }


//    protected <T extends UpdateOperation> Set<Entity> getObject( final Class<T> operationClass ) {
//        Set<Entity> set = new HashSet<Entity>();
//        if ( operationClass == null)
//            throw new IllegalStateException( "OperationClass can't be null" );
//        Collection<? extends UpdateOperation> it= getOperations( operationClass);
//        for (UpdateOperation next:it ) {
//            // FIXME
//            String currentId =next.getCurrentId();
//            final Entity current = getLastKnown(currentId);
//            set.add( current);
//        }
//        return set;
//    }

    public Set<EntityReferencer.ReferenceInfo> getRemovedReferences()
    {
        return removedReferences;
    }

    public Set<Entity> getChangeObjects() {
        return changed;
    }

    public Set<Entity> getAddObjects() {
        return added;
    }


    public boolean isModified(RaplaType raplaType)
    {
        return modified.contains( raplaType) ;
    }

    public boolean isModified() {
        return !removedReferences.isEmpty() || !changed.isEmpty() || !added.isEmpty() || switchTemplateMode;
    }

    public boolean isEmpty() {
        return !isModified() && timeInterval == null;
    }


    public void setInvalidateInterval(TimeInterval timeInterval)
    {
        this.timeInterval = timeInterval;
    }

    public TimeInterval getInvalidateInterval()
    {
        return timeInterval;
    }


    public void setSwitchTemplateMode(boolean b)
    {
        switchTemplateMode = b;
    }

    public boolean isSwitchTemplateMode() {
        return switchTemplateMode;
    }

    public void addChanged(Entity changed)
    {
        this.changed.add(changed);
        modified.add( changed.getRaplaType());
    }
}