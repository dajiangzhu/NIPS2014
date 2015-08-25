import java.util.ArrayList;
import java.util.List;

import edu.uga.DICCCOL.DicccolUtilIO;
import edu.uga.liulab.djVtkBase.djNiftiData;

public class FindComponent {
	public int subID = -1;
	List<djNiftiData> GLMTemList = null;
	public double TemThreshold = 3.5;
	public double ComThreshold = 0.01;

	public void initialGLMTemplate() {
		GLMTemList = new ArrayList<djNiftiData>();
		GLMTemList.add(new djNiftiData("/home/dzhu/workspace/NIPS2014/GLM/sub"
				+ subID + "_MOTOR_c6_thres4.nii.gz"));
		GLMTemList.add(new djNiftiData("/home/dzhu/workspace/NIPS2014/GLM/sub"
				+ subID + "_MOTOR_c19_thres4.nii.gz"));
		GLMTemList.add(new djNiftiData("/home/dzhu/workspace/NIPS2014/GLM/sub"
				+ subID + "_MOTOR_c1_thres4.nii.gz"));
		GLMTemList.add(new djNiftiData("/home/dzhu/workspace/NIPS2014/GLM/sub"
				+ subID + "_MOTOR_c3_thres4.nii.gz"));
	}

	public void locateComponent() {
		List<String> outPutList = new ArrayList<String>();

		for (int c = 0; c < 400; c++) {
			System.out.println("Calculating component: " + c);
			System.out.println("Loading home/dzhu/workspace/NIPS2014/volume/"
					+ subID + "/MOTOR/component_" + (c + 1) + ".nii...");
			djNiftiData curCom = new djNiftiData(
					"/home/dzhu/workspace/NIPS2014/volume/" + subID
							+ "/MOTOR/component_" + (c + 1) + ".nii");
			String line = "";
			int xSize = curCom.xSize;
			int ySize = curCom.ySize;
			int zSize = curCom.zSize;
			for (int i = 0; i < this.GLMTemList.size(); i++) {
				djNiftiData curGLMTem = this.GLMTemList.get(i);
				double countT = 0;
				double countC = 0;
				double countTC = 0;

				for (int x = 0; x < xSize; x++)
					for (int y = 0; y < ySize; y++)
						for (int z = 0; z < zSize; z++) {
							double valueT = curGLMTem
									.getValueBasedOnVolumeCoordinate(x, y, z, 0);
							double valueC = curCom
									.getValueBasedOnVolumeCoordinate(x, y, z, 0);
							if (valueT > TemThreshold)
								countT++;
							if (valueC > ComThreshold)
								countC++;
							if (valueT > TemThreshold && valueC > ComThreshold)
								countTC++;
						} // for x,y,z
				double tmpResult = countTC
						/ ((countT - countTC) + (countC - countTC));
				line += tmpResult + " ";
			} // for all GML templates
			outPutList.add(line);
		} // for 400 components
		DicccolUtilIO.writeArrayListToFile(outPutList, "sub" + subID
				+ "_cardRateInfo.txt");
	}

	public void anaCardRateInfo() {
		for (int subID = 7; subID < 11; subID++) {
			double[][] curRate = DicccolUtilIO.loadFileAsArray("sub" + subID
					+ "_cardRateInfo.txt", 400, 4);
			double[][] maxRate = new double[4][2];
			for (int c = 0; c < 4; c++) {
				for (int row = 0; row < 400; row++) {
					if (curRate[row][c] > maxRate[c][1]) {
						maxRate[c][1] = curRate[row][c];
						maxRate[c][0] = row;
					} //if
				} //for row
			} //for c
			DicccolUtilIO.writeArrayToFile(maxRate, 4, 2, " ", "sub"+subID+"_anaRateInfo.txt");
		} //for all subjects
	}

	public static void main(String[] args) {
		FindComponent mainHandler = new FindComponent();
//		mainHandler.subID = Integer.valueOf(args[0]);
//		System.out.println("Dealting with subject:" + mainHandler.subID);
//		mainHandler.initialGLMTemplate();
//		mainHandler.locateComponent();
		mainHandler.anaCardRateInfo();

	}

}
