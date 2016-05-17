package models;

import java.util.List;

public class CidadeService {
    /**
     * Create an cidade
     *
     * @param Cidade data
     *
     * @return Cidade
     */
    public static Cidade create(Cidade data) {
        return CidadeDAO.create(data);
    }

    /**
     * Update an cidade
     *
     * @param Cidade data
     *
     * @return Cidade
     */
    public static Cidade update(Cidade data) {
        return CidadeDAO.update(data);
    }

    /**
     * Find an cidade by id
     *
     * @param Integer id
     *
     * @return Cidade
     */
    public static Cidade find(Long id) {
        return CidadeDAO.find(id);
    }

    /**
     * Delete an cidade by id
     *
     * @param Integer id
     */
    public static Boolean delete(Long id) {
        Cidade cidade = CidadeDAO.find(id);
        if (cidade != null) {
            CidadeDAO.delete(id);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get all cidades
     *
     * @return List<Cidade>
     */
    public static List<Cidade> all() {
        return CidadeDAO.all();
    }

    /**
     * Get the page of cidades
     *
     * @param Integer page
     * @param Integer size
     *
     * @return List<Cidade>
     */
    public static List<Cidade> paginate(Integer page, Integer size) {
        return CidadeDAO.paginate(page, size);
    }

    /**
     * Get the number of total of cidades
     *
     * @return Long
     */
    public static Long count() {
        return CidadeDAO.count();
    }
}