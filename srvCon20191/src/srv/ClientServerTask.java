package srv;

import java.io.FileWriter;
import java.io.PrintWriter;

import uniandes.gload.core.Task;

public class ClientServerTask extends Task {

	@Override
	public void execute() {		
		try {
			ClienteCifrado clienteCifrado = new ClienteCifrado();
			long tiempo = clienteCifrado.darTiempoEscrituraOk() - clienteCifrado.darTiempoLecturaLlave();
			writeTxT(tiempo + "");
			success();
			} catch(Exception e) {
			fail();
			e.printStackTrace();
		}
	}
	
	public void success() {
		System.out.println(Task.OK_MESSAGE);
	}
	
	public void fail() {	
		try {
			PrintWriter pw = new PrintWriter(new FileWriter("./datos/fail.txt", true));
			pw.println("1");
			pw.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println(Task.MENSAJE_FAIL);
	}
	
	public void writeTxT(String data) {
		try {
			PrintWriter pw = new PrintWriter(new FileWriter("./datos/datos.txt", true));
			pw.println(data);
			pw.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
