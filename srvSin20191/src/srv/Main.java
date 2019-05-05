package srv;

import java.io.IOException;

public class Main {

	public static void main(String[] args) {
		int maximo = 100;
		// TODO Auto-generated method stub
		for(int i = 0; i< maximo; i++)
		{
			ClienteSinSeguridad cliente = new ClienteSinSeguridad();
			try {
				cliente.main(null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
