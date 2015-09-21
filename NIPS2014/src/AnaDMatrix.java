import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import edu.uga.DICCCOL.DicccolUtilIO;
import edu.uga.DICCCOL.stat.Correlation;

public class AnaDMatrix {
	public void anaLogs() {
		String files;

		List<String> distributionInfo = new ArrayList<String>();
		for (int subID = 1; subID < 69; subID++) {
			File folder = new File(
					"/ifs/loni/faculty/thompson/four_d/dzhu/Journal_ESL/logs/MOTOR/MultipleRun/"
							+ subID);
			File[] listOfFiles = folder.listFiles();
			String fileFilter = "SCC_MR";
			String tmpLine = "";
			DescriptiveStatistics stats = new DescriptiveStatistics();
			double sumError = 0.0;
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					files = listOfFiles[i].getName();
					if (files.startsWith(fileFilter)) {
						System.out.println("Dealling with " + files);
						List<String> currentFileContent = DicccolUtilIO
								.loadFileToArrayList("/ifs/loni/faculty/thompson/four_d/dzhu/Journal_ESL/logs/MOTOR/MultipleRun/"
										+ subID + "/" + files);
						for (int r = 0; r < currentFileContent.size(); r++) {
							if (currentFileContent.get(r).startsWith(
									"Total Decode Error")) {
								double tmpError = Double
										.valueOf(currentFileContent.get(r)
												.split("\\s+")[4]);
								System.out.println(tmpError);
								stats.addValue(tmpError);
							} // if
						} // for r
					} // if
				} // if
			} // for all the files
			tmpLine += stats.getStandardDeviation() + " ";
			tmpLine += stats.getMean() + " ";
			tmpLine += stats.getPercentile(50) + " ";
//			tmpLine += (stats.getMean() + stats.getStandardDeviation());
			tmpLine += stats.getMax();
			distributionInfo.add(tmpLine);
		} // for every subject
		DicccolUtilIO
				.writeArrayListToFile(
						distributionInfo,
						"/ifs/loni/faculty/thompson/four_d/dzhu/Journal_ESL/logs/errorLimit_Motor_1_68_0921.txt");
	}

	public double[][] trsposeM(double[][] data, int row, int column) {
		double[][] result = new double[column][row];
		for (int i = 0; i < row; i++)
			for (int j = 0; j < column; j++)
				result[j][i] = data[i][j];
		return result;
	}

	public void calSingleSubSingleRunCorre() {
		int dicSize = 400;
		int tSize = 284;
		Correlation correlation = new Correlation();
		for (int subID = 1; subID < 11; subID++) {
			for (int round = 0; round < 10; round++) {
				double[][] currentCorreM = new double[dicSize][dicSize];
				String currentFile = "./DMatrix/" + subID
						+ ".MOTOR_Dmat_dsize_400_lamda_8non_r_" + round
						+ "_.txt";
				System.out.println("Dealling with " + currentFile);
				double[][] currentSigM = this.trsposeM(DicccolUtilIO
						.loadFileAsArray(currentFile, tSize, dicSize), tSize,
						dicSize);
				for (int i = 0; i < dicSize; i++)
					for (int j = 0; j < dicSize; j++)
						currentCorreM[i][j] = correlation.Correlation_Pearsons(
								currentSigM[i], currentSigM[j]);
				DicccolUtilIO.writeArrayToFile(currentCorreM, dicSize, dicSize,
						" ", "./DMatrixCorre/sub_" + subID + "_r_" + round
								+ "_corre.txt");
				DicccolUtilIO.writeVtkMatrix1(currentCorreM, dicSize, dicSize,
						"./DMatrixCorre/sub_" + subID + "_r_" + round
								+ "_corre.vtk");
			} // for all rounds
		} // for every subject
	}

	public List<Integer> initialSigIndexList(int size) {
		List<Integer> newList = new ArrayList<Integer>();
		for (int i = 0; i < size; i++)
			newList.add(i);
		return newList;
	}

	public List<Integer> updateSigIndexList(List<Integer> currentList,
			double[][] seedSigM, double[][] currentSigM,
			Correlation correlation, int dicSize) {
		List<Integer> updateList = new ArrayList<Integer>();
		double correLationThreshold = 0.85;
		for (int i = 0; i < currentList.size(); i++) {
			int tmpSigIndex = currentList.get(i);
			for (int d = 0; d < dicSize; d++) {
				double tmpCorreValue = correlation.Correlation_Pearsons(
						seedSigM[tmpSigIndex], currentSigM[d]);
				if (tmpCorreValue >= correLationThreshold) {
					updateList.add(tmpSigIndex);
					// System.out.println("Preserve "+tmpSigIndex+"...");
					break;
				}
			} // for all dictionary items
		} // for i
		return updateList;
	}

	public List<Integer> finalizeStableSigIndex(List<Integer> currentIndexList,
			double[][] data, Correlation correlation, int dicSize) {
		List<Integer> finalList = new ArrayList<Integer>();
		double correLationThreshold = 0.2;
		do {
			finalList.add(currentIndexList.get(0));
			for (int i = 1; i < currentIndexList.size(); i++) {
				double tmpCorreValue = correlation.Correlation_Pearsons(
						data[currentIndexList.get(0)],
						data[currentIndexList.get(i)]);
				if (tmpCorreValue >= correLationThreshold) {
					currentIndexList.remove(i);
					i--;
				}
			} // for
			currentIndexList.remove(0);
		} while (currentIndexList.size() > 1);
		if (currentIndexList.size() == 1)
			finalList.add(currentIndexList.get(0));
		return finalList;
	}

	public void findSingleSubInitialTemplateSig() {
		int dicSize = 400;
		int tSize = 284;
		Correlation correlation = new Correlation();
		List<List<Integer>> allStableSigIndexList = new ArrayList<List<Integer>>();
		int seedRound = 2;
		for (int subID = 1; subID < 11; subID++) {
			String seedFile = "./DMatrix/" + subID
					+ ".MOTOR_Dmat_dsize_400_lamda_8non_r_" + seedRound
					+ "_.txt";
			System.out.println("Dealling with " + seedFile);
			List<Integer> stableSigIndexList = this.initialSigIndexList(400);
			double[][] seedSigM = this.trsposeM(
					DicccolUtilIO.loadFileAsArray(seedFile, tSize, dicSize),
					tSize, dicSize);
			for (int round = 0; round < 10; round++) {
				if (round != seedRound) {
					String currentFile = "./DMatrix/" + subID
							+ ".MOTOR_Dmat_dsize_400_lamda_8non_r_" + round
							+ "_.txt";
					// System.out.println("Loading " + currentFile);
					double[][] currentSigM = this.trsposeM(DicccolUtilIO
							.loadFileAsArray(currentFile, tSize, dicSize),
							tSize, dicSize);
					stableSigIndexList = this.updateSigIndexList(
							stableSigIndexList, seedSigM, currentSigM,
							correlation, dicSize);
				}
			} // for all rounds except the first round
			System.out.println("Total " + stableSigIndexList.size() + ": "
					+ stableSigIndexList);
			allStableSigIndexList.add(stableSigIndexList);
		} // for every subject

		List<List<Integer>> finalStableSigIndexList = new ArrayList<List<Integer>>();
		for (int subID = 1; subID < 11; subID++) {
			String currentFile = "./DMatrix/" + subID
					+ ".MOTOR_Dmat_dsize_400_lamda_8non_r_" + seedRound
					+ "_.txt";
			System.out.println("Finalizing with " + currentFile
					+ "---------------");
			double[][] currentSigM = this.trsposeM(
					DicccolUtilIO.loadFileAsArray(currentFile, tSize, dicSize),
					tSize, dicSize);
			System.out.println("For sub " + subID + ", ther are original "
					+ allStableSigIndexList.get(subID - 1).size()
					+ " stable signals...");
			List<Integer> currentFinalizeList = this.finalizeStableSigIndex(
					allStableSigIndexList.get(subID - 1), currentSigM,
					correlation, dicSize);
			System.out.println("Now, there are " + currentFinalizeList.size()
					+ " template signals...");
			double[][] finalSigTemplate = new double[currentFinalizeList.size()][tSize];
			for (int i = 0; i < currentFinalizeList.size(); i++)
				finalSigTemplate[i] = currentSigM[currentFinalizeList.get(i)];
			DicccolUtilIO.writeArrayToFile(finalSigTemplate,
					currentFinalizeList.size(), tSize, " ",
					"./DMatrixTemplate/" + subID + ".MOTOR_Dmat_Template.txt");
			double[][] currentCorreM = new double[currentFinalizeList.size()][currentFinalizeList
					.size()];
			for (int i = 0; i < currentFinalizeList.size(); i++)
				for (int j = 0; j < currentFinalizeList.size(); j++)
					currentCorreM[i][j] = correlation.Correlation_Pearsons(
							finalSigTemplate[i], finalSigTemplate[j]);
			DicccolUtilIO.writeArrayToFile(currentCorreM,
					currentFinalizeList.size(), currentFinalizeList.size(),
					" ", "./DMatrixTemplate/sub_" + subID
							+ ".MOTOR_Template_corre.txt");
			DicccolUtilIO.writeVtkMatrix1(currentCorreM,
					currentFinalizeList.size(), currentFinalizeList.size(),
					"./DMatrixTemplate/sub_" + subID
							+ ".MOTOR_Template_corre.vtk");
		} // for every subject

	}

	public void test() {
		double[][] currentSigM = this.trsposeM(
				DicccolUtilIO.loadFileAsArray("test.txt", 3, 5), 3, 5);
		DicccolUtilIO.writeArrayToFile(currentSigM, 5, 3, " ", "testT.txt");
	}

	public static void main(String[] args) {
		AnaDMatrix mainHandler = new AnaDMatrix();
		mainHandler.anaLogs();
		// mainHandler.calSingleSubSingleRunCorre();
		// mainHandler.findSingleSubInitialTemplateSig();

	}

}