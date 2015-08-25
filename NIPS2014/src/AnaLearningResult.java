import java.util.ArrayList;
import java.util.List;

import edu.uga.DICCCOL.DicccolUtilIO;
import edu.uga.DICCCOL.stat.Correlation;


public class AnaLearningResult {
	public int index = -1;
	int[][] inforTable;
	int[] maxRound;
	public int dicSize = 400;
	public int tSize = 284;
	
	
	public double[][] trsposeM(double[][] data, int row, int column) {
		double[][] result = new double[column][row];
		for (int i = 0; i < row; i++)
			for (int j = 0; j < column; j++)
				result[j][i] = data[i][j];
		return result;
	}
	
	
	public void collectInfor() {
		System.out.println("initialAllSigM...");
		inforTable = new int[10][index+1];
		maxRound = new int[index+1];
		for (int subID = 1; subID < 11; subID++) {
			// read the record file for seedSub's startIndex and round
			String subRecordFile = "./optimizedDMatrix/sub"+subID+"/sub_" + subID
					+ "_record.txt";
			System.out.println("subRecordFile: " + subRecordFile);
			List<String> subRecordContent = DicccolUtilIO
					.loadFileToArrayList(subRecordFile);
			for(int i=0;i<=index;i++)
			{
				int tmpMaxRound = -1;
				for(int l=0;l<subRecordContent.size();l++)
				{
					String currentLine = subRecordContent.get(l);
					String[] currentLineContent = currentLine.split("\\s+");
					int nIndex = Integer.valueOf(currentLineContent[0].trim());
					if(nIndex==i)
					{
						int nRound = Integer.valueOf(currentLineContent[1].trim());
//						double dError = Double.valueOf(currentLineContent[2].trim());
						if(nRound>tmpMaxRound)
							tmpMaxRound = nRound;	
					} //if
				} //for all the lines
				inforTable[subID-1][i] = tmpMaxRound;
				System.out.println("index: "+i+" tmpMaxRound: "+tmpMaxRound);				
			} //for all the index
		} // for all sub
		
		for(int i=0;i<=index;i++)
		{
			int tmpMaxRound = -1;
			for(int subID = 1; subID < 11; subID++)
				if(inforTable[subID-1][i]>tmpMaxRound)
					tmpMaxRound = inforTable[subID-1][i];
			maxRound[i] = tmpMaxRound;
		} //for all the index		
	}
	
	public void calculateCorre()
	{
		Correlation correlation = new Correlation();
		for(int i=0;i<=index;i++)
		{
			List<String> outCorreList = new ArrayList<String>();
			String correLine = "";
			double[][] templateSigM = this.trsposeM(
					DicccolUtilIO.loadFileAsArray("./optimizedDMatrix/templateSig_index_"+i+".txt", tSize, 1),
					tSize, 1);
			for(int r=0;r<=maxRound[i];r++)
			{
				double[][] currentDataM = new double[10][tSize];
				for (int subID = 1; subID < 11; subID++)
				{
					int currentRound = r;
					if(r>inforTable[subID-1][i])
						currentRound = inforTable[subID-1][i];
					double[][] tmpSubM = this.trsposeM(
							DicccolUtilIO.loadFileAsArray("./optimizedDMatrix/sub"+subID+"/sub_"+subID+"_index_"+i+"_round_"+currentRound+"_D.txt", tSize, dicSize),
							tSize, dicSize);
					currentDataM[subID-1] = tmpSubM[i];					
				} //for all the subjects
				DicccolUtilIO.writeArrayToFile( this.trsposeM(currentDataM, 10, tSize)  , tSize, 10, " ", "./optimizedDMatrix/alaResult/groupSig_index_"+i+"_round_"+r+".txt");
				
				double currentCorre = 0.0;
				for(int subID = 1; subID < 11; subID++)
					currentCorre += correlation.Correlation_Pearsons(templateSigM[0], currentDataM[subID-1]);
				currentCorre /= 10.0;
				correLine += currentCorre+" ";				
			} //for all the rounds
			outCorreList.add(correLine);
			DicccolUtilIO.writeArrayListToFile(outCorreList, "./optimizedDMatrix/alaResult/correList_index_"+i+".txt");
		} //for all the index
	}

	public static void main(String[] args) {
		int index = Integer.valueOf(args[0].trim());
		AnaLearningResult mainHandler = new AnaLearningResult();
		mainHandler.index = index;
		mainHandler.collectInfor();
		mainHandler.calculateCorre();

	}

}
