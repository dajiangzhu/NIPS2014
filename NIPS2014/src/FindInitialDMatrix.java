import java.io.File;
import java.util.ArrayList;
import java.util.List;
import edu.uga.DICCCOL.DicccolUtilIO;
import edu.uga.DICCCOL.stat.Correlation;

public class FindInitialDMatrix {

	public double[][] tmpSigs = null;
	public int tSize = 284;
	public int dicSize = 400;
	public double threshold = 0.72;

	public double[][] trsposeM(double[][] data, int row, int column) {
		double[][] result = new double[column][row];
		for (int i = 0; i < row; i++)
			for (int j = 0; j < column; j++)
				result[j][i] = data[i][j];
		return result;
	}

	public void initialTmpSigs() {
		tmpSigs = new double[4][this.tSize];
		for (int i = 0; i < 4; i++) {
			String subFile = "/ifs/loni/faculty/thompson/four_d/dzhu/IPMI2015/previousTmpSigs/templateSig_index_" + i
					+ ".txt";
			double[][] tmpSig = this.trsposeM(
					DicccolUtilIO.loadFileAsArray(subFile, tSize, 1), tSize, 1);
			tmpSigs[i] = tmpSig[0];
		}
	}

	public void anaLogs(int startSubID,int endSubID) {
		String files;
		Correlation correlation = new Correlation();

		List<String> candidateDMatrixList = new ArrayList<String>();
		for (int subID = startSubID; subID < endSubID; subID++) {
			File folder = new File(
					"/ifs/loni/faculty/thompson/four_d/dzhu/data/HCP/TaskFMRI/multipleRuns/"
							+ subID);
			File[] listOfFiles = folder.listFiles();
			String fileFilter = ".txt";

			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					files = listOfFiles[i].getName();
					if (files.endsWith(fileFilter)) {
						System.out.println("Dealling with " + files);
						double[][] curSubSigM = this.trsposeM(DicccolUtilIO
								.loadFileAsArray(
										"/ifs/loni/faculty/thompson/four_d/dzhu/data/HCP/TaskFMRI/multipleRuns/"
												+ subID + "/" + files, tSize,
										dicSize), tSize, dicSize);
						
						boolean[] flag = new boolean[4];
						for (int t = 0; t < 4; t++) {
							flag[t]=false;
							for (int d = 0; d < this.dicSize; d++) {
								double tmpCorreValue = correlation
										.Correlation_Pearsons(this.tmpSigs[t],curSubSigM[d]);
								if(tmpCorreValue>threshold)
									flag[t] = true;
							} //for all dictionary atoms
						} //for all t
						if(flag[0]&&flag[1]&&flag[2]&&flag[3])
							candidateDMatrixList.add(files);
					} // if
				} // if
			} // for all the files

		} // for every subject
		DicccolUtilIO.writeArrayListToFile(candidateDMatrixList,
				"/ifs/loni/faculty/thompson/four_d/dzhu/IPMI2015/candidateDMatrixList");
	}

	public static void main(String[] args) {
		int startSubID = Integer.valueOf(args[0]);
		int endSubID = Integer.valueOf(args[1]);
		
		FindInitialDMatrix mainHandler = new FindInitialDMatrix();
		mainHandler.threshold = Double.valueOf(args[2]);
		mainHandler.initialTmpSigs();
		mainHandler.anaLogs(startSubID,endSubID);

	}

}
