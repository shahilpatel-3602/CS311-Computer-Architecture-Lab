package generic;

import processor.Clock;
import processor.Processor;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import generic.Statistics;

public class Simulator {
		
	static Processor processor;
	static boolean simulationComplete;
	
	public static void setupSimulation(String assemblyProgramFile, Processor p)
	{
		Simulator.processor = p;
		loadProgram(assemblyProgramFile);

		//initially set simulation complete to false as the simulation hasn't completed
		simulationComplete = false;
	}
	
	static void loadProgram(String assemblyProgramFile)
	{
		try {
			// create a reader
			// System.out.println(assemblyProgramFile);
			FileInputStream file = new FileInputStream(assemblyProgramFile);
			BufferedInputStream reader = new BufferedInputStream(file);

			// read one byte at a time
			int currentPC = 0; //Stores current PC value

			int ch; //used for reading lines from file
			int linecounter = 0; //used to keep track of number of lines read from assembly file
			byte[] oneline = new byte[4]; //temporary location to store an instruction from assembly file

			int Nd = -1; //Bytes 0 to Nd of memory have global data stored in them;
			int Nt = Nd + 1; //Bytes Nd + 1 to Nt of memory have text/code segment stored in them

			//below code sets PC to the memory address of the first instruction
			if((ch = reader.read(oneline, 0, 4)) != -1){
				currentPC = ByteBuffer.wrap(oneline).getInt();
				processor.getRegisterFile().setProgramCounter(currentPC);
			}

			//writing global data to main memory
			while (true) {
				if(linecounter >= currentPC)
					break;
				ch = reader.read(oneline, 0, 4);
				Nd += 1;
				int num = ByteBuffer.wrap(oneline).getInt(); //temporary integer to store one integer of global data
				processor.getMainMemory().setWord(Nd, num);
				Nt = Nd;
				linecounter += 1;
			}

			//writing instructions to main memory
			while (((ch = reader.read(oneline, 0, 4)) != -1)) {
				Nt += 1;
				int ins = ByteBuffer.wrap(oneline).getInt(); //temporary integer to store one instruction
				processor.getMainMemory().setWord(Nt, ins);
				linecounter += 1;
			}

			processor.getRegisterFile().setValue(0, 0);
			processor.getRegisterFile().setValue(1, 65535);
			processor.getRegisterFile().setValue(2, 65535);

			// close the reader
			reader.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static void simulate()
	{
		Statistics.setNumberOfInstructions(0);
		Statistics.setNumberOfCycles(0);

		while(!simulationComplete)
		{
			//executing instructions according to given in the question
			//perfrom RW
			processor.getRWUnit().performRW();
			
			// perform MA
			processor.getMAUnit().performMA();
			
			//perform EX
			processor.getEXUnit().performEX();
			
			//perform OF
			processor.getOFUnit().performOF();
			
			//perform IF
			processor.getIFUnit().performIF();
			Clock.incrementClock();

			Statistics.setNumberOfInstructions(Statistics.getNumberOfInstructions() + 1);
			Statistics.setNumberOfCycles(Statistics.getNumberOfCycles() + 1);
		}
	}
	
	public static void setSimulationComplete(boolean value)
	{
		simulationComplete = value;
	}
}
