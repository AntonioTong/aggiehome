package main;


import java.util.List;

public class Database {

	private String time;
	private Overall overall;
	private Power power;
	private List<CellIndv> cellCollect;
	
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public Overall getOverall() {
		return overall;
	}
	public void setOverall(Overall overall) {
		this.overall = overall;
	}
	public Power getPower() {
		return power;
	}
	public void setPower(Power power) {
		this.power = power;
	}
	public List<CellIndv> getCellCollect() {
		return cellCollect;
	}
	public void setCellCollect(List<CellIndv> cellCollect) {
		this.cellCollect = cellCollect;
	}

}

class Overall{
	private String vMin, vMax, tMax, tMin, vPack, cPack;

	public String getvMin() {
		return vMin;
	}

	public void setvMin(String vMin) {
		this.vMin = vMin;
	}

	public String getvMax() {
		return vMax;
	}

	public void setvMax(String vMax) {
		this.vMax = vMax;
	}

	public String gettMax() {
		return tMax;
	}

	public void settMax(String tMax) {
		this.tMax = tMax;
	}

	public String gettMin() {
		return tMin;
	}

	public void settMin(String tMin) {
		this.tMin = tMin;
	}

	public String getvPack() {
		return vPack;
	}

	public void setvPack(String vPack) {
		this.vPack = vPack;
	}

	public String getcPack() {
		return cPack;
	}

	public void setcPack(String cPack) {
		this.cPack = cPack;
	}


}
class Power{
	private String pB, pG, pH,pP;

	public String getpB() {
		return pB;
	}

	public void setpB(String pB) {
		this.pB = pB;
	}

	public String getpG() {
		return pG;
	}

	public void setpG(String pG) {
		this.pG = pG;
	}

	public String getpH() {
		return pH;
	}

	public void setpH(String pH) {
		this.pH = pH;
	}

	public String getpP() {
		return pP;
	}

	public void setpP(String pP) {
		this.pP = pP;
	}
}
class CellCollect {
	private List<CellIndv> indivCell;

	public List<CellIndv> getIndivCell() {
		return indivCell;
	}

	public void setIndivCell(List<CellIndv> indivCell) {
		this.indivCell = indivCell;
	}
	
}
class CellIndv{
	private String cellNumber;
	private String cellCur;
	private String cellVol;
	private String cellTemp;
	private String cellSocC;
	private String cellSohP;
	private String cellSohC;
	private String cellBalance;

	public String getCellCur() {
		return cellCur;
	}
	public void setCellCur(String cellCur) {
		this.cellCur = cellCur;
	}
	public String getCellVol() {
		return cellVol;
	}
	public void setCellVol(String cellVol) {
		this.cellVol = cellVol;
	}
	public String getCellTemp() {
		return cellTemp;
	}
	public void setCellTemp(String cellTemp) {
		this.cellTemp = cellTemp;
	}
	public String getCellSocC() {
		return cellSocC;
	}
	public void setCellSocC(String cellSocC) {
		this.cellSocC = cellSocC;
	}
	public String getCellSohP() {
		return cellSohP;
	}
	public void setCellSohP(String cellSohP) {
		this.cellSohP = cellSohP;
	}
	public String getCellSohC() {
		return cellSohC;
	}
	public void setCellSohC(String cellSohC) {
		this.cellSohC = cellSohC;
	}
	public String getCellBalance() {
		return cellBalance;
	}
	public void setCellBalance(String cellBalance) {
		this.cellBalance = cellBalance;
	}
	public String getCellNumber() {
		return cellNumber;
	}
	public void setCellNumber(String cellNumber) {
		this.cellNumber = cellNumber;
	}
	
	
}