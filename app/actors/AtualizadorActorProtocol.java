package actors;

public class AtualizadorActorProtocol {

	public static class AtualizaIniciativasEScores {

		private final String dataVotada;
		private final String identificadorUnicoDoServidor;

		public AtualizaIniciativasEScores(String dataVotada, String identificadorUnicoDoServidor) {
			this.dataVotada = dataVotada;
			this.identificadorUnicoDoServidor = identificadorUnicoDoServidor;
		}

		public String getIdentificadorUnicoDoServidor() {
			return identificadorUnicoDoServidor;
		}

		public String getDataVotada() {
			return dataVotada;
		}
		
	}
}