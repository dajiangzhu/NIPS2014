import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.uga.DICCCOL.DicccolUtilIO;
import edu.uga.DICCCOL.stat.Correlation;

public class FindTemplateDictionaryItem {

	public boolean successful = true;
	public double correlationLowThreshold = 0.1;
	public double correlationHighThreshold = 1.0;
	public int dicSize = 400;
	public int tSize = 284;
	public List<double[][]> allSigM = new ArrayList<double[][]>();
	public List<double[][]> allTemplateSigM = new ArrayList<double[][]>();
	public int startIndex = -1;
	
	public int subStart = -1;
	public int subEnd = -1;
	
	
	public double[][] getSigAtIdex(int index)
	{
		double[][] sigAtIndex = new double[10][tSize];
		for (int subID = 1; subID < 11; subID++)
		{
			double[][] currentSigM = this.allSigM.get(subID-1);
			sigAtIndex[subID-1] = currentSigM[index];			
		}
		return sigAtIndex;
	}
	
	public void initialAllTemplateSigM()
	{
		String currentFileName;
		File folder = new File("./optimizedDMatrix");
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				currentFileName = listOfFiles[i].getName();
				if (currentFileName.startsWith("templateSig_"+this.subStart+"_"+this.subEnd) && currentFileName.endsWith(".txt")) {
					System.out.println("Loading "+currentFileName);
					double[][] templateSigM = this.trsposeM(
							DicccolUtilIO.loadFileAsArray("./optimizedDMatrix/"+currentFileName, tSize, 1),
							tSize, 1);
					allTemplateSigM.add(templateSigM);
				} // if
			} // if
		} // for all the files
	}

	public void initialAllSigM() {
		System.out.println("initialAllSigM...");
		for (int subID = this.subStart; subID < this.subEnd; subID++) {
			// read the record file for seedSub's startIndex and round
			String subRecordFile = "./optimizedDMatrix/sub"+subID+"/sub_" + subID
					+ "_record.txt";
			System.out.println("subRecordFile: " + subRecordFile);
			List<String> subRecordContent = DicccolUtilIO
					.loadFileToArrayList(subRecordFile);
			String[] strLastRecord = subRecordContent.get(
					subRecordContent.size() - 1).split("\\s+");
			int fileIndexPart = Integer.valueOf(strLastRecord[0].trim());
			int round = Integer.valueOf(strLastRecord[1].trim());

			// load the updated D-Matrix of the seedSub from the previous round
			// optimization. startIndex=0 and round=0 when the first time do
			// this
			String subFile = "./optimizedDMatrix/sub" + subID + "/sub_" + subID
					+ "_index_" + fileIndexPart + "_round_" + round + "_D.txt";
			System.out.println("subFile: " + subFile);
			double[][] subSigM = this.trsposeM(
					DicccolUtilIO.loadFileAsArray(subFile, tSize, dicSize),
					tSize, dicSize);
			allSigM.add(subSigM);
		} // for all sub
	}

	public double[][] trsposeM(double[][] data, int row, int column) {
		double[][] result = new double[column][row];
		for (int i = 0; i < row; i++)
			for (int j = 0; j < column; j++)
				result[j][i] = data[i][j];
		return result;
	}

	public List<Integer> initialSigIndexList(int size) {
		List<Integer> newList = new ArrayList<Integer>();
		for (int i = 0; i < size; i++)
			newList.add(i);
		return newList;
	}

	public List<Integer> updateSigIndexList(List<Integer> currentList,
			double[][] seedSigM, double[][] currentSigM, Correlation correlation) {
		List<Integer> updateList = new ArrayList<Integer>();
		for (int i = startIndex; i < currentList.size(); i++) {  //***************might be i=0???
			int tmpSigIndex = currentList.get(i);
			for (int d = startIndex; d < dicSize; d++) {
				double tmpCorreValue = correlation.Correlation_Pearsons(
						seedSigM[tmpSigIndex], currentSigM[d]);
				if (tmpCorreValue >= correlationHighThreshold) {
					updateList.add(tmpSigIndex);
					// System.out.println("Preserve "+tmpSigIndex+"...");
					break;
				}
			} // for all dictionary items
		} // for i
		return updateList;
	}
	
	List<Integer> checkDuplicateTemplateSig(
			List<Integer> candidateSigIndexList, double[][] seedSigM,
			Correlation correlation) {
		List<Integer> passList = new ArrayList<Integer>();
		if (startIndex == 0)
			passList.addAll(candidateSigIndexList);
		else {
			for (int c = 0; c < candidateSigIndexList.size(); c++) {
				boolean isValid = true;
				for (int t = 0; t < this.allTemplateSigM.size(); t++) {
					double tmpCorreValue = correlation
							.Correlation_Pearsons(
									seedSigM[candidateSigIndexList.get(c)],
									this.allTemplateSigM.get(t)[0]);
					if (tmpCorreValue >= correlationLowThreshold)
						isValid = false;
				} // for t
				if (isValid)
					passList.add(candidateSigIndexList.get(c));
			} // for c
		} // else
		return passList;
	}

//	List<Integer> checkDuplicateTemplateSig(
//			List<Integer> candidateSigIndexList, double[][] seedSigM,
//			Correlation correlation) {
//		List<Integer> passList = new ArrayList<Integer>();
//		if (startIndex == 0)
//			passList.addAll(candidateSigIndexList);
//		else {
//			for (int c = 0; c < candidateSigIndexList.size(); c++) {
//				boolean isValid = true;
//				for (int t = 0; t < startIndex; t++) {
//					double tmpCorreValue = correlation
//							.Correlation_Pearsons(
//									seedSigM[candidateSigIndexList.get(c)],
//									seedSigM[t]);
//					if (tmpCorreValue >= correlationLowThreshold)
//						isValid = false;
//				} // for t
//				if (isValid)
//					passList.add(candidateSigIndexList.get(c));
//			} // for c
//		} // else
//		return passList;
//	}

	public void printIndexTable(int[][] table, int row, int column) {
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < column; j++)
				System.out.print(table[i][j] + " ");
			System.out.println();
		}
	}

	public void printCorreTable(double[] table, int column) {
		for (int i = 0; i < column; i++)
			System.out.print(table[i] + " ");
		System.out.println();
	}

	public void findTemplateSig() {
		Correlation correlation = new Correlation();
		int[][] allDictionaryIndexTable = new int[10][10];
		double[] allCorrelationTable = new double[10];

		// =================================Section I Find the template
		// Signal=========================================//
		System.out.println("For all possible seed subject...");
		for (int seedSubID = 1; seedSubID < 11; seedSubID++) {
			System.out.println("seedSub: " + seedSubID);
			double[][] seedSubSigM = this.allSigM.get(seedSubID-1);
			List<Integer> candidateSigIndexList = this.initialSigIndexList(400);

			// ************Step 1****************//
			for (int currentSubID = 1; currentSubID < 11; currentSubID++) {
				if (currentSubID != seedSubID) {
					double[][] currentSubSigM = this.allSigM.get(currentSubID-1);
					candidateSigIndexList = this.updateSigIndexList(
							candidateSigIndexList, seedSubSigM, currentSubSigM,
							correlation);
				} // if currentSubID!=seedSubID
			} // for currentSubID
			System.out.println("candidateSigIndexList size - "
					+ candidateSigIndexList.size() + ": "
					+ candidateSigIndexList);
			if (candidateSigIndexList.size() == 0)
				continue;

			// ************Step 2****************//
			// check the candidate signals are not similar to the fixed ones
			List<Integer> currentFinalCandatiteList = this
					.checkDuplicateTemplateSig(candidateSigIndexList,
							seedSubSigM, correlation);
			System.out.println("currentFinalCandatiteList size - "
					+ currentFinalCandatiteList.size() + ": "
					+ currentFinalCandatiteList);
			if (currentFinalCandatiteList.size() == 0)
				continue;

			// ************Step 3****************//
			// calculate the seed-centered correlation and record the dictionary
			// index of each subject
			int[][] currentSeedCenteredIndexTable = new int[currentFinalCandatiteList
					.size()][10];
			double[] currentSeedCenteredCorrelationTable = new double[currentFinalCandatiteList
					.size()];
			for (int i = 0; i < currentFinalCandatiteList.size(); i++) {
				currentSeedCenteredIndexTable[i][seedSubID - 1] = currentFinalCandatiteList
						.get(i);
				for (int currentSubID = 1; currentSubID < 11; currentSubID++)
					if (currentSubID != seedSubID) {
						double[][] currentSubSigM = this.allSigM
								.get(currentSubID-1);
						double maxCorre = -1.0;
						int maxItemIndex = -1;
						for (int d = startIndex; d < dicSize; d++) {
							double tmpCorre = correlation.Correlation_Pearsons(
									seedSubSigM[currentFinalCandatiteList
											.get(i)], currentSubSigM[d]);
							if (tmpCorre > maxCorre) {
								maxCorre = tmpCorre;
								maxItemIndex = d;
							} // if
						} // for all items in the current subject
						currentSeedCenteredIndexTable[i][currentSubID - 1] = maxItemIndex;
						currentSeedCenteredCorrelationTable[i] += maxCorre;
					} // if currentSubID!=seedSubID
			} // for all the final current candidate index

			// ************Step 4****************//
			// find the combination with the max correlation
			double maxGroupCorre = 0.1;
			int maxGroupIndex = -1;
			for (int i = 0; i < currentFinalCandatiteList.size(); i++)
				if (currentSeedCenteredCorrelationTable[i] > maxGroupCorre) {
					maxGroupCorre = currentSeedCenteredCorrelationTable[i];
					maxGroupIndex = i;
				}
			if (maxGroupIndex == -1)
				continue;
			allDictionaryIndexTable[seedSubID - 1] = currentSeedCenteredIndexTable[maxGroupIndex];
			allCorrelationTable[seedSubID - 1] = maxGroupCorre;
		} // for all possible seedSubID, to find the optimal template.
		System.out.println("Printing allDictionaryIndexTable...");
		this.printIndexTable(allDictionaryIndexTable, 10, 10);
		System.out.println("Printing allCorrelationTable...");
		this.printCorreTable(allCorrelationTable, 10);

		// =================================Section II Move the template signal
		// and the corresponding ones (other subjects) the correct location
		// (startIndex)=========================================//
		// ************Step 5****************//
		// find the template signal with the max correlation
		double maxGroupCorre = 0.1;
		int maxGroupIndex = -1;
		for (int i = 0; i < 10; i++)
			if (allCorrelationTable[i] > maxGroupCorre) {
				maxGroupCorre = allCorrelationTable[i];
				maxGroupIndex = i;
			}
		if (maxGroupIndex == -1) {
			this.successful = false;
			System.out.println("The allCorrelationTable are all zeros!!!");
			return;
		}
		System.out.println("maxGroupIndex: "+maxGroupIndex);

		// ************Step 6****************//
		// save the template signal and update the D matrix for all the subjects
		List<String> outTemplateSig = new ArrayList<String>();
		int oriRow = allDictionaryIndexTable[maxGroupIndex][maxGroupIndex];
		for (int i = 0; i < tSize; i++) {
			String newLine = "";
			newLine += this.allSigM.get(maxGroupIndex)[oriRow][i];
			outTemplateSig.add(newLine);
		}
		DicccolUtilIO.writeArrayListToFile(outTemplateSig,
				"./optimizedDMatrix/templateSig_"+this.subStart+"_"+this.subEnd+"_index_" + this.startIndex+ ".txt");
		DicccolUtilIO.writeArrayListToFile(outTemplateSig,
				"./optimizedDMatrix/BackTemplateSig_"+this.subStart+"_"+this.subEnd+"_index_" + this.startIndex
						+ "_sub_" + (maxGroupIndex + 1) + "_oriIndex_" + oriRow
						+ ".txt");

		DicccolUtilIO.writeArrayToFile( this.trsposeM(this.getSigAtIdex(startIndex), 10, tSize)  , tSize, 10, " ", "./optimizedDMatrix/sig_index_"+this.subStart+"_"+this.subEnd+"_"+startIndex+"_before.txt");
		for (int subID = 1; subID < 11; subID++) {
			double[][] currentSigM = this.allSigM.get(subID-1);
			double[] swapData = new double[tSize];
			swapData = currentSigM[startIndex];
			currentSigM[startIndex] = currentSigM[allDictionaryIndexTable[maxGroupIndex][subID - 1]];
			currentSigM[allDictionaryIndexTable[maxGroupIndex][subID - 1]] = swapData;
			double[][] currentSigM_T = this.trsposeM(currentSigM, dicSize,
					tSize);
			DicccolUtilIO.writeArrayToFile(currentSigM_T, tSize, dicSize, " ",
					"./optimizedDMatrix/sub" + (subID+this.subStart-1) + "/sub_" + (subID+this.subStart-1)
							+ "_index_" + startIndex + "_round_0_D.txt");
		} // for all the subjects
		DicccolUtilIO.writeArrayToFile( this.trsposeM(this.getSigAtIdex(startIndex), 10, tSize)  , tSize, 10, " ", "./optimizedDMatrix/sig_index_"+this.subStart+"_"+this.subEnd+"_"+startIndex+"_after.txt");
	}

	public void updateExcuteState() {
		List<String> outStatus = new ArrayList<String>();
		if(this.successful)
			outStatus.add("good");
		else
			outStatus.add("bad");
		DicccolUtilIO.writeArrayListToFile(outStatus,
				"findTemplateSig_status.txt");
	}

	public static void main(String[] args) {
		System.out
				.println("Enter JAVA process for finding the template dictionary item  #################################################");
//		if (args.length == 1) {
			int startIndex = 2; //Integer.valueOf(args[0]);
			System.out.println("Will deale with index = " + startIndex);
			FindTemplateDictionaryItem mainHandler = new FindTemplateDictionaryItem();
			mainHandler.startIndex = startIndex;
			mainHandler.subStart = 11;
			mainHandler.subEnd = 21;
			mainHandler.initialAllSigM();
			mainHandler.initialAllTemplateSigM();
			mainHandler.correlationHighThreshold = 0.65;
			double correlationHighThresholdLimit = 0.4;
			double delta_corre = 0.01;
			do{
				mainHandler.correlationHighThreshold -= delta_corre;
				System.out.println("Trying correlationHighThreshold = "+mainHandler.correlationHighThreshold);
				mainHandler.successful = true;

				mainHandler.findTemplateSig();
				mainHandler.updateExcuteState();
			}while(!mainHandler.successful && mainHandler.correlationHighThreshold>=correlationHighThresholdLimit);
//		} else
//			System.out.println("Please input correct paramaters: index(0-399)");

	}

}
