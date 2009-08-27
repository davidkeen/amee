package com.amee.admin.service.app;

import com.amee.domain.AMEEStatus;
import com.amee.domain.Pager;
import com.amee.domain.site.App;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;

@Service
class AppServiceDAO implements Serializable {

    @PersistenceContext
    private EntityManager entityManager;

    // Apps

    @SuppressWarnings(value = "unchecked")
    public App getAppByUid(String uid) {
        App app = null;
        if (uid != null) {
            List<App> apps = entityManager.createQuery(
                    "SELECT a " +
                            "FROM App a " +
                            "WHERE a.uid = :uid " +
                            "AND a.status != :trash")
                    .setParameter("trash", AMEEStatus.TRASH)
                    .setParameter("uid", uid)
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", "query.appService")
                    .getResultList();
            if (apps.size() > 0) {
                app = apps.get(0);
            }
        }
        return app;
    }

    @SuppressWarnings(value = "unchecked")
    public App getAppByName(String name) {
        App app = null;
        if (name != null) {
            List<App> apps = entityManager.createQuery(
                    "SELECT a " +
                            "FROM App a " +
                            "WHERE a.name = :name " +
                            "AND a.status != :trash")
                    .setParameter("trash", AMEEStatus.TRASH)
                    .setParameter("name", name.trim())
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.cacheRegion", "query.appService")
                    .getResultList();
            if (apps.size() > 0) {
                app = apps.get(0);
            }
        }
        return app;
    }

    @SuppressWarnings(value = "unchecked")
    public List<App> getApps(Pager pager) {
        // first count all apps
        long count = (Long) entityManager.createQuery(
                "SELECT count(a) " +
                        "FROM App a " +
                        "WHERE a.status != :trash")
                .setParameter("trash", AMEEStatus.TRASH)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.appService")
                .getSingleResult();
        // tell pager how many apps there are and give it a chance to select the requested page again
        pager.setItems(count);
        pager.goRequestedPage();
        // now get the apps for the current page
        List<App> apps = entityManager.createQuery(
                "SELECT a " +
                        "FROM App a " +
                        "WHERE a.status != :trash " +
                        "ORDER BY a.name")
                .setParameter("trash", AMEEStatus.TRASH)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.appService")
                .setMaxResults(pager.getItemsPerPage())
                .setFirstResult((int) pager.getStart())
                .getResultList();
        // update the pager
        pager.setItemsFound(apps.size());
        // all done, return results
        return apps;
    }

    @SuppressWarnings(value = "unchecked")
    public List<App> getApps() {
        return (List<App>) entityManager.createQuery(
                "SELECT a " +
                        "FROM App a " +
                        "WHERE a.status != :trash " +
                        "ORDER BY a.name")
                .setParameter("trash", AMEEStatus.TRASH)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", "query.appService")
                .getResultList();
    }

    public void save(App app) {
        entityManager.persist(app);
    }

    public void remove(App app) {
        entityManager.remove(app);
    }
}
