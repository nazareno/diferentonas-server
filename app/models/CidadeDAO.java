package models;

import java.util.List;

import play.db.jpa.JPA;

public class CidadeDAO {
    /**
     * Create an Cidade
     *
     * @param Cidade model
     *
     * @return Cidade
     */
    public static Cidade create (Cidade model) {
//        model.emptyToNull();
        JPA.em().persist(model);
        // Flush and refresh for check
        JPA.em().flush();
        JPA.em().refresh(model);
        return model;
    }

    /**
     * Find an Cidade by id
     *
     * @param Integer id
     *
     * @return Cidade
     */
    public static Cidade find(Long id) {
        return JPA.em().find(Cidade.class, id);
    }

    /**
     * Update an Cidade
     *
     * @param Cidade model
     *
     * @return Cidade
     */
    public static Cidade update(Cidade model) {
        return JPA.em().merge(model);
    }

    /**
     * Delete an Cidade by id
     *
     * @param Integer id
     */
    public static void delete(Long id) {
        Cidade model = JPA.em().getReference(Cidade.class, id);
        JPA.em().remove(model);
    }

    /**
     * Get all Cidades
     *
     * @return List<Cidade>
     */
    public static List<Cidade> all() {
        return JPA.em().createQuery("SELECT m FROM " + Cidade.TABLE + " m ORDER BY id", Cidade.class).getResultList();
    }

    /**
     * Get the page of Cidades
     *
     * @param Integer page
     * @param Integer size
     *
     * @return List<Cidade>
     */
    public static List<Cidade> paginate(Integer page, Integer size) {
        return JPA.em().createQuery("SELECT m FROM " + Cidade.TABLE + " m ORDER BY id", Cidade.class).setFirstResult(page*size).setMaxResults(size).getResultList();
    }

    /**
     * Get the number of total row
     *
     * @return Long
     */
    public static Long count() {
        return JPA.em().createQuery("SELECT count(m) FROM " + Cidade.TABLE + " m", Long.class).getSingleResult();
    }
}