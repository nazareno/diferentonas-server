package actors;
public class AtualizadorActorProtocol {

    public static class AtualizaScores { }

    public static class AtualizaIniciativas {
        public final String name;

        public AtualizaIniciativas(String name) {
            this.name = name;
        }
    }
}