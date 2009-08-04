package com.amee.service.environment;

import com.amee.domain.AMEEStatus;
import com.amee.domain.APIVersion;
import com.amee.domain.Pager;
import com.amee.domain.data.DataCategory;
import com.amee.domain.environment.Environment;
import com.amee.domain.profile.Profile;
import com.amee.domain.site.Site;
import com.amee.service.ThreadBeanHolder;
import com.amee.service.data.DataService;
import com.amee.service.profile.ProfileService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;

@Service
public class EnvironmentService implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    private static final String CACHE_REGION = "query.environmentService";

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private DataService dataService;

    @Autowired
    private ProfileService profileService;
    
    // Events

    @SuppressWarnings(value = "unchecked")
    public void beforeEnvironmentDelete(Environment environment) {
        log.debug("beforeEnvironmentDelete");
        List<Profile> profiles = entityManager.createQuery(
                "SELECT p " +
                        "FROM Profile p " +
                        "WHERE p.environment.id = :environmentId " +
                        "AND p.status = :active")
                .setParameter("environmentId", environment.getId())
                .setParameter("active", AMEEStatus.ACTIVE)
                .getResultList();
        for (Profile profile : profiles) {
            profileService.remove(profile);
        }

        // trash root DataCategories
        List<DataCategory> dataCategories = entityManager.createQuery(
                "UPDATE DataCategory " +
                        "SET status = :trash, " +
                        "modified = current_timestamp() " +
                        "WHERE environment.id = :environmentId " +
                        "AND dataCategory IS NULL " +
                        "AND status = :active")
                .setParameter("trash", AMEEStatus.TRASH)
                .setParameter("active", AMEEStatus.ACTIVE)
                .setParameter("environmentId", environment.getId())
                .getResultList();
        for (DataCategory dataCategory : dataCategories) {
            dataService.remove(dataCategory);
        }
    }

    public void beforeSiteDelete(Site site) {
        log.debug("beforeSiteDelete");
    }

    // Environments

    @SuppressWarnings(value = "unchecked")
    public Environment getEnvironmentByUid(String uid) {
        Environment environment = null;
        if (!StringUtils.isBlank(uid)) {
            Session session = (Session) entityManager.getDelegate();
            Criteria criteria = session.createCriteria(Environment.class);
            criteria.add(Restrictions.naturalId().set("uid", uid));
            criteria.add(Restrictions.eq("status", AMEEStatus.ACTIVE));
            criteria.setCacheable(true);
            criteria.setCacheRegion(CACHE_REGION);
            List<Environment> environments = criteria.list();
            if (environments.size() == 1) {
                log.debug("getEnvironmentByUid() found: " + uid);
                environment = environments.get(0);
            } else {
                log.debug("getEnvironmentByUid() NOT found: " + uid);
            }
        }
        return environment;
    }

    @SuppressWarnings(value = "unchecked")
    public List<Environment> getEnvironments() {
        List<Environment> environments = entityManager.createQuery(
                "FROM Environment e " +
                        "WHERE e.status = :active")
                .setParameter("active", AMEEStatus.ACTIVE)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getResultList();
        return environments;
    }

    @SuppressWarnings(value = "unchecked")
    public List<Environment> getEnvironments(Pager pager) {
        // first count all environments
        long count = (Long) entityManager.createQuery(
                "SELECT count(e) " +
                        "FROM Environment e " +
                        "WHERE e.status = :active")
                .setParameter("active", AMEEStatus.ACTIVE)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getSingleResult();
        // tell pager how many environments there are and give it a chance to select the requested page again
        pager.setItems(count);
        pager.goRequestedPage();
        // now get the environments for the current page
        List<Environment> environments = entityManager.createQuery(
                "SELECT e " +
                        "FROM Environment e " +
                        "WHERE e.status = :active " +
                        "ORDER BY e.name")
                .setParameter("active", AMEEStatus.ACTIVE)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .setMaxResults(pager.getItemsPerPage())
                .setFirstResult((int) pager.getStart())
                .getResultList();
        // update the pager
        pager.setItemsFound(environments.size());
        // all done, return results
        return environments;
    }

    public void save(Environment environment) {
        entityManager.persist(environment);
    }

    public void remove(Environment environment) {
        beforeEnvironmentDelete(environment);
        environment.setStatus(AMEEStatus.TRASH);
    }

    public static Environment getEnvironment() {
        return (Environment) ThreadBeanHolder.get("environment");
    }

    // API Versions

    @SuppressWarnings(value = "unchecked")
    public List<APIVersion> getAPIVersions() {
        List<APIVersion> apiVersions = entityManager.createQuery(
                "FROM APIVersion av " +
                        "WHERE av.status = :active " +
                        "ORDER BY av.version")
                .setParameter("active", AMEEStatus.ACTIVE)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getResultList();
        return apiVersions;
    }

    public APIVersion getAPIVersion(String version) {
        return (APIVersion) entityManager.createQuery(
                "FROM APIVersion av " +
                        "WHERE av.version = :version " +
                        "AND av.status = :active")
                .setParameter("version", version)
                .setParameter("active", AMEEStatus.ACTIVE)
                .setHint("org.hibernate.cacheable", true)
                .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                .getSingleResult();
    }
}