package org.vaadin.addons.lazyquerycontainer.example.jpa;

import java.util.List;

import javax.persistence.EntityManager;

public class TaskService {
	private EntityManager entityManager;

	public TaskService(EntityManager entityManager) {
		this.entityManager=entityManager;
	}

	protected int countTasks() {
        javax.persistence.Query query = entityManager.createQuery("SELECT count(t) from Task as t");           
        return (int)((Long) query.getSingleResult()).longValue();
	}
	
	@SuppressWarnings("unchecked")
	protected List<Task> loadTasks(int startIndex, int count, Object[] sortPropertyIds, boolean[] sortStates) {
		
		String criteria=" ORDER BY t.name ASC";
		 
        for(int i=0;i<sortPropertyIds.length;i++) {
            if(i==0) {
                    criteria=" ORDER BY";
            } else {
                    criteria+=",";
            }
            criteria+=" t."+sortPropertyIds[i];
            if(sortStates[i]) {
                    criteria+=" ASC";
            }
            else {
                    criteria+=" DESC";                              
            }
        }
		
        javax.persistence.Query query = entityManager.createQuery("SELECT t from Task as t"+criteria);
        query.setFirstResult(startIndex);
        query.setMaxResults(count);
        
		return query.getResultList();
	}

	protected void saveTasks(List<Task> addedItems, List<Task> modifiedItems,List<Task> removedItems) {
		entityManager.getTransaction().begin();
        for(Task task : addedItems) {
        	entityManager.persist(task);
        }
        for(Task task : modifiedItems) {
        	entityManager.persist(task);
        }
        for(Task task : removedItems) {
            entityManager.remove(task);
        }
        entityManager.getTransaction().commit();
	}

	
}
