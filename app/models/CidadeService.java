package models;

import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class CidadeService {

	private CidadeDAO dao;

	@Inject
	public CidadeService(CidadeDAO dao) {
		this.dao = dao;
	}

	/**
	 * Create an cidade
	 *
	 * @param Cidade data
	 *
	 * @return Cidade
	 */
	public Cidade create(Cidade data) {
		return dao.create(data);
	}

	/**
	 * Update an cidade
	 *
	 * @param Cidade data
	 *
	 * @return Cidade
	 */
	public Cidade update(Cidade data) {
		return dao.update(data);
	}

	/**
	 * Find an cidade by id
	 *
	 * @param Integer id
	 *
	 * @return Cidade
	 */
	public Cidade find(Long id) {
		return dao.find(id);
	}

	/**
	 * Delete an cidade by id
	 *
	 * @param Integer id
	 */
	public Boolean delete(Long id) {
		Cidade cidade = dao.find(id);
		if (cidade != null) {
			dao.delete(id);
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
	public List<Cidade> all() {
		return dao.all();
	}

	/**
	 * Get the page of cidades
	 *
	 * @param Integer page
	 * @param Integer size
	 *
	 * @return List<Cidade>
	 */
	public List<Cidade> paginate(Integer page, Integer size) {
		return dao.paginate(page, size);
	}

	/**
	 * Get the number of total of cidades
	 *
	 * @return Long
	 */
	public Long count() {
		return dao.count();
	}
}