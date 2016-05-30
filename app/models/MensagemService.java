package models;

import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MensagemService {

	private MensagemDAO dao;

	@Inject
	public MensagemService(MensagemDAO dao) {
		this.dao = dao;
	}

	/**
	 * Create an mensagem
	 *
	 * @param Mensagem data
	 *
	 * @return Mensagem
	 */
	public Mensagem create(Mensagem data) {
		return dao.create(data);
	}

	/**
	 * Update an mensagem
	 *
	 * @param Mensagem data
	 *
	 * @return Mensagem
	 */
	public Mensagem update(Mensagem data) {
		return dao.update(data);
	}

	/**
	 * Find an mensagem by id
	 *
	 * @param Integer id
	 *
	 * @return Mensagem
	 */
	public Mensagem find(Long id) {
		return dao.find(id);
	}

	/**
	 * Delete an mensagem by id
	 *
	 * @param Integer id
	 */
	public Boolean delete(Long id) {
		Mensagem mensagem = dao.find(id);
		if (mensagem != null) {
			dao.delete(id);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Get all mensagems
	 *
	 * @return List<Mensagem>
	 */
	public List<Mensagem> all() {
		return dao.all();
	}

	/**
	 * Get the page of mensagems
	 *
	 * @param Integer page
	 * @param Integer size
	 *
	 * @return List<Mensagem>
	 */
	public List<Mensagem> paginate(Integer page, Integer size) {
		return dao.paginate(page, size);
	}

	/**
	 * Get the number of total of mensagems
	 *
	 * @return Long
	 */
	public Long count() {
		return dao.count();
	}
}