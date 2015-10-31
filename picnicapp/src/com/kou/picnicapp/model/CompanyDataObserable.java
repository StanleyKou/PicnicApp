/*
 * KT Roadshow 1.1.1
 *
 *  Copyright ⓒ 2015 kt corp. All rights reserved.
 *
 *  This is a proprietary software of kt corp, and you may not use this file except in
 *  compliance with license agreement with kt corp. Any redistribution or use of this
 *  software, with or without modification shall be strictly prohibited without prior written
 *  approval of kt corp, and the copyright notice above does not evidence any actual or
 *  intended publication of such software.
 */

package com.kou.picnicapp.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class CompanyDataObserable extends Observable {

	private ArrayList<TargetData> companyDataList = new ArrayList<TargetData>();

	private Gson gson = new Gson();

	private String savePath;
	private String filename;

	public CompanyDataObserable(String savePath, String filename) {
		this.savePath = savePath;
		this.filename = filename;

		File saveFile = new File(savePath + "/" + filename);
		if (!saveFile.exists()) {
			try {
				saveFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		loadFromFile();

	}

	public ArrayList<TargetData> getCompanyDataList() {
		return companyDataList;
	}

	public void addTarget(TargetData companyData) {

		companyDataList.add(companyData);
		// Collections.sort(companyDataList, new Comparator<CompanyData>() {
		// @Override
		// public int compare(CompanyData lhs, CompanyData rhs) {
		// if (lhs.getCompanyName() == null || rhs.getCompanyName() == null) {
		// return 0;
		// }
		// return lhs.getCompanyName().compareTo(rhs.getCompanyName());
		//
		// }
		// });

		saveToFile();

		setChanged();
		notifyObservers();
	}

	public void setCompanyDataList(ArrayList<TargetData> companyDataList) {
		this.companyDataList.clear();
		this.companyDataList.addAll(companyDataList);

		saveToFile();

		setChanged();
		notifyObservers();
	}

	public void removeCompany(TargetData item) {
		companyDataList.remove(item);

		saveToFile();

		setChanged();
		notifyObservers();
	}

	public int getSize() {
		return companyDataList.size();

	}

	public void loadFromFile() {

		if (companyDataList.size() != 0) {
			return;
		}

		String fromFile = readFromFile(savePath, filename);

		if (fromFile != null && fromFile.length() > 0) {

			java.lang.reflect.Type listType = new TypeToken<List<TargetData>>() {
			}.getType();
			ArrayList<TargetData> pushDataList = gson.fromJson(fromFile, listType);
			setCompanyDataList(pushDataList);
		}

	}

	public void saveToFile() {
		String strToSave = gson.toJson(companyDataList);
		writeToFile(strToSave, savePath, filename);
	}

	private void writeToFile(String stringData, String localPath, String filename) {
		try {
			BufferedWriter bos = new BufferedWriter(new FileWriter(localPath + "/" + filename));
			bos.write(stringData);
			bos.flush();
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String readFromFile(String localPath, String filename) {

		String everything = "";

		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(localPath + "/" + filename));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				line = br.readLine();
			}
			everything = sb.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return everything;
	}
}