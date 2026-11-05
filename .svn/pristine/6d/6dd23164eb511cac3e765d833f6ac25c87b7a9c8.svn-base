package com.knot.provisioning.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class DashBoardSummaryDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@JsonInclude(Include.NON_NULL)
	private String title;

	private String chartType;

	private List<String> rootLabel;

	private List<ChartDataSummaryDTO> chartDataSets;

	public DashBoardSummaryDTO() {
		super();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getChartType() {
		return chartType;
	}

	public void setChartType(String chartType) {
		this.chartType = chartType;
	}

	public List<String> getRootLabel() {
		return rootLabel;
	}

	public void setRootLabel(List<String> rootLabel) {
		this.rootLabel = rootLabel;
	}

	public List<ChartDataSummaryDTO> getChartDataSets() {
		return chartDataSets;
	}

	public void setChartDataSets(List<ChartDataSummaryDTO> chartDataSets) {
		this.chartDataSets = chartDataSets;
	}

}
