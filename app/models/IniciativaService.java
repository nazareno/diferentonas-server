package models;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class IniciativaService {

    private IniciativaDAO dao;

    @Inject
    public IniciativaService(IniciativaDAO dao) {
        this.dao = dao;
    }

    public Iniciativa find(Long id) {
        return dao.find(id);
    }

}
