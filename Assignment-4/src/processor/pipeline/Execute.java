package processor.pipeline;

import generic.Instruction;
import generic.Operand;
import generic.Statistics;
import jdk.dynalink.beans.StaticClass;
import processor.Processor;
import generic.Instruction.OperationType;
import generic.Operand.OperandType;

import java.util.HashSet;
import java.util.Set;

public class Execute {
	Processor containingProcessor;
	OF_EX_LatchType OF_EX_Latch;
	EX_MA_LatchType EX_MA_Latch;
	EX_IF_LatchType EX_IF_Latch;
	IF_OF_LatchType IF_OF_Latch;
	IF_EnableLatchType IF_EnableLatch;
	
	public Execute(Processor containingProcessor, OF_EX_LatchType oF_EX_Latch, EX_MA_LatchType eX_MA_Latch, EX_IF_LatchType eX_IF_Latch, IF_OF_LatchType iF_OF_Latch, IF_EnableLatchType iF_EnableLatch)
	{
		this.containingProcessor = containingProcessor;
		this.OF_EX_Latch = oF_EX_Latch;
		this.EX_MA_Latch = eX_MA_Latch;
		this.EX_IF_Latch = eX_IF_Latch;
		this.IF_OF_Latch = iF_OF_Latch;
		this.IF_EnableLatch = iF_EnableLatch;
	}
	
	public void performEX()
	{
		Instruction cmd = null;
		
		//Check if the EX Stage is locked or not, if locked then set the MA stage Lock to true and disable the lock in EX stage.
		if(OF_EX_Latch.isEX_Locked()){
			EX_MA_Latch.setMA_Lock(true);
			OF_EX_Latch.setEX_Lock(false);
			EX_MA_Latch.setInstruction(null);
		}

		else if(OF_EX_Latch.isEX_enable()){
			Instruction currentInstruction = OF_EX_Latch.getInstruction();

			//using abbrevations to shorten the code
			Instruction CI;
			CI = currentInstruction;

			int currentPC = currentInstruction.getProgramCounter() - 1;

			int CP;
			CP = currentPC;

			OperationType currentOperation = currentInstruction.getOperationType();
			int sourceOperand1 = -1, sourceOperand2 = -1, immediate, remainder;
			int aluResult = -1;

			OperationType CO;
			CO = currentOperation;

			//Creating set of branch instructions and end instruction
			Set<String> BranchInstructions = new HashSet<String>();
			//Adding the branch instructions
			BranchInstructions.add("jmp");
			BranchInstructions.add("beq");
			BranchInstructions.add("bne");
			BranchInstructions.add("blt");
			BranchInstructions.add("bgt");
			BranchInstructions.add("end");

			//To handle the control hazard we check if the current operation is branch, if it is a branch instruction then disable the IF, OF and EX stage (control interlocks)
			if(BranchInstructions.contains(currentOperation.name())){

				Statistics.setNumberOfBranchesTaken(Statistics.getNumberOfBranchesTaken() + 2);
				IF_EnableLatch.setIF_enable(false);
				IF_OF_Latch.setOF_enable(false);
				OF_EX_Latch.setEX_enable(false);

			}

			switch (currentOperation){

				case add:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					int rs1;
					rs1 = sourceOperand1;

					sourceOperand2 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand2().getValue());
					int rs2;
					rs2 = sourceOperand2;

					aluResult = rs1 + rs2;
					break;
				
				case sub:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					rs1 = sourceOperand1;

					sourceOperand2 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand2().getValue());
					rs2 = sourceOperand2;

					aluResult = rs1 - rs2;
					break;
				
				case mul:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					rs1 = sourceOperand1;
					
					sourceOperand2 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand2().getValue());
					rs2 = sourceOperand2;

					aluResult = sourceOperand1 * sourceOperand2;
					break;
				
				case div:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					rs1 = sourceOperand1;

					sourceOperand2 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand2().getValue());
					rs2 = sourceOperand2;

					aluResult = sourceOperand1 / sourceOperand2;
					remainder = (sourceOperand1 % sourceOperand2);
					containingProcessor.getRegisterFile().setValue(31, remainder);
					break;
				
				case and:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					rs1 = sourceOperand1;

					sourceOperand2 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand2().getValue());
					rs2 = sourceOperand2;

					aluResult = sourceOperand1 & sourceOperand2;
					break;

				
				case or:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					rs1 = sourceOperand1;
					
					sourceOperand2 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand2().getValue());
					rs2 = sourceOperand2;

					aluResult = sourceOperand1 | sourceOperand2;
					break;
				
				case xor:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					rs1 = sourceOperand1;

					sourceOperand2 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand2().getValue());
					rs2 = sourceOperand2;

					aluResult = sourceOperand1 ^ sourceOperand2;
					break;
				
				case slt:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					rs1 = sourceOperand1;

					sourceOperand2 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand2().getValue());
					rs2 = sourceOperand2;

					if(sourceOperand1 < sourceOperand2)
						aluResult = 1;
					else
						aluResult = 0;
					break;

				case sll:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					rs1 = sourceOperand1;

					sourceOperand2 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand2().getValue());
					rs2 = sourceOperand2;

					aluResult = sourceOperand1 << sourceOperand2;
					break;
				
				case srl:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					rs1 = sourceOperand1;

					sourceOperand2 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand2().getValue());
					rs2 = sourceOperand2;

					aluResult = sourceOperand1 >>> sourceOperand2;
					break;
				
				case sra:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					rs1 = sourceOperand1;

					sourceOperand2 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand2().getValue());
					rs2 = sourceOperand2;

					aluResult = sourceOperand1 >> sourceOperand2;
					break;

				case load:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					rs1 = sourceOperand1;

					immediate = currentInstruction.getSourceOperand2().getValue();
					int imx = immediate;
					aluResult = sourceOperand1 + immediate;
					break;

				case store:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getDestinationOperand().getValue());
					rs1 = sourceOperand1;

					immediate = currentInstruction.getSourceOperand2().getValue();
					imx = immediate;
					aluResult = sourceOperand1 + immediate;
					break;

				case jmp:
					OperandType jump = currentInstruction.getDestinationOperand().getOperandType();
					OperandType j;
					j = jump;
					if(j == OperandType.Register){
						immediate = containingProcessor.getRegisterFile().getValue(currentInstruction.getDestinationOperand().getValue());
					}
					else{
						immediate = currentInstruction.getDestinationOperand().getValue();
					}
					aluResult = currentPC + immediate;
					EX_IF_Latch.setEX_IF_enable(true, aluResult);
					break;

				case beq:

					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					rs1 = sourceOperand1;

					sourceOperand2 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand2().getValue());
					rs2 = sourceOperand2;

					immediate = currentInstruction.getDestinationOperand().getValue();
					imx = immediate;

					if(rs1 == rs2){
						aluResult = currentPC + immediate;
						EX_IF_Latch.setEX_IF_enable(true, aluResult);
					}
					break;

				case bne:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					rs1 = sourceOperand1;

					sourceOperand2 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand2().getValue());
					rs2 = sourceOperand2;

					immediate = currentInstruction.getDestinationOperand().getValue();
					imx = immediate;

					if(rs1 != rs2){
						aluResult = currentPC + immediate;
						EX_IF_Latch.setEX_IF_enable(true, aluResult);
					}
					break;

				case blt:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					rs1 = sourceOperand1;

					sourceOperand2 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand2().getValue());
					rs2 = sourceOperand2;

					immediate = currentInstruction.getDestinationOperand().getValue();
					imx = immediate;

					if(rs1 < rs2){
						aluResult = currentPC + immediate;
						EX_IF_Latch.setEX_IF_enable(true, aluResult);
					}
					break;

				case bgt:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					rs1 = sourceOperand1;
					
					sourceOperand2 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand2().getValue());
					rs2 = sourceOperand2;

					immediate = currentInstruction.getDestinationOperand().getValue();
					imx = immediate;

					if(rs1 > rs2){
						aluResult = currentPC + immediate;
						EX_IF_Latch.setEX_IF_enable(true, aluResult);
					}
					break;

				case addi:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					// int rs1;
					rs1 = sourceOperand1;

					immediate = currentInstruction.getSourceOperand2().getValue();
					// int imx;
					imx = immediate;
					aluResult = rs1 + immediate;
					break;

				case subi:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					rs1 = sourceOperand1;
					immediate = currentInstruction.getSourceOperand2().getValue();

					aluResult = sourceOperand1 - immediate;
					break;

				case muli:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					rs1 = sourceOperand1;

					immediate = currentInstruction.getSourceOperand2().getValue();
					aluResult = sourceOperand1 * immediate;
					break;

				case divi:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					rs1 = sourceOperand1;

					immediate = currentInstruction.getSourceOperand2().getValue();
					imx = immediate;

					aluResult = sourceOperand1 / immediate;
					remainder = (sourceOperand1 % immediate);
					containingProcessor.getRegisterFile().setValue(31, remainder);
					break;
				
				case andi:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					rs1 = sourceOperand1;

					immediate = currentInstruction.getSourceOperand2().getValue();

					aluResult = sourceOperand1 & immediate;
					break;
				
				case ori:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					rs1 = sourceOperand1;
					immediate = currentInstruction.getSourceOperand2().getValue();

					aluResult = sourceOperand1 | immediate;
					break;

				case xori:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					rs1 = sourceOperand1;

					immediate = currentInstruction.getSourceOperand2().getValue();
					aluResult = sourceOperand1 ^ immediate;
					break;

				case slti:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					rs1 = sourceOperand1;

					immediate = currentInstruction.getSourceOperand2().getValue();
					imx = immediate;

					if(sourceOperand1 < immediate)
						aluResult = 1;
					else
						aluResult = 0;
					break;

				case slli:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					rs1 = sourceOperand1;

					immediate = currentInstruction.getSourceOperand2().getValue();
					imx = immediate;
					aluResult = sourceOperand1 << immediate;
					break;

				case srli:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					rs1 = sourceOperand1;

					immediate = currentInstruction.getSourceOperand2().getValue();
					imx = immediate;

					aluResult = sourceOperand1 >>> immediate;
					break;

				case srai:
					sourceOperand1 = containingProcessor.getRegisterFile().getValue(currentInstruction.getSourceOperand1().getValue());
					rs1 = sourceOperand1;
					
					immediate = currentInstruction.getSourceOperand2().getValue();
					aluResult = sourceOperand1 >> immediate;
					break;

				case end:
					break;
				default:
					break;
			}
			EX_MA_Latch.setAluResult(aluResult);
			EX_MA_Latch.setInstruction(currentInstruction);

			EX_MA_Latch.setMA_enable(true);

			//Alu result will be -1 if the instruction was branch
			if(aluResult != -1)
				System.out.println("\nEX Stage: " + "Current PC: " + currentPC + " rs1: " + sourceOperand1 + " rs2: " + sourceOperand2 + " Alu Result: " + aluResult);
			else
				System.out.println("\nEX Stage: " + "Current PC: " + currentPC + " rs1: " + sourceOperand1 + " rs2: " + sourceOperand2);
		}
	}
}
