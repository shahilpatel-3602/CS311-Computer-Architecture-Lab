package processor.pipeline;

import processor.Processor;

public class InstructionFetch {
	
	Processor containingProcessor;
	IF_EnableLatchType IF_EnableLatch;
	IF_OF_LatchType IF_OF_Latch;
	EX_IF_LatchType EX_IF_Latch;
	
	public InstructionFetch(Processor containingProcessor, IF_EnableLatchType iF_EnableLatch, IF_OF_LatchType iF_OF_Latch, EX_IF_LatchType eX_IF_Latch)
	{
		this.containingProcessor = containingProcessor;
		this.IF_EnableLatch = iF_EnableLatch;
		this.IF_OF_Latch = iF_OF_Latch;
		this.EX_IF_Latch = eX_IF_Latch;
	}
	
	public void performIF()
	{
		if(IF_EnableLatch.isIF_enable())
		{
			if(EX_IF_Latch.isEX_IF_enable()) {
				int branchPC = EX_IF_Latch.getPC();
				int BP;
				BP = branchPC;
				containingProcessor.getRegisterFile().setProgramCounter(BP);

				EX_IF_Latch.setEX_IF_enable(false);
			}

			int currentPC = containingProcessor.getRegisterFile().getProgramCounter();
			int CP;
			int NI;
			int newInstruction = containingProcessor.getMainMemory().getWord(currentPC);
			
			//dead
			int x = 100;
			if(x>10001){
				x--;
			}
			else if(x==2000){
				x++;
			}

			CP = currentPC;
			NI = newInstruction;

			IF_OF_Latch.setInstruction(newInstruction);
			containingProcessor.getRegisterFile().setProgramCounter(currentPC + 1);
			
			//IF_EnableLatch.setIF_enable(false);
			IF_OF_Latch.setOF_enable(true);

			System.out.println("\nIF Stage" + " Current PC: " + currentPC);
		}
	}

}
