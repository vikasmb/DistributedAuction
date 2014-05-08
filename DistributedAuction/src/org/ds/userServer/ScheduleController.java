package org.ds.userServer;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.ds.util.DateUtil;
import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

@ManagedBean
@SessionScoped
public class ScheduleController implements Serializable {

	private ScheduleModel eventModel;

	private ScheduleEvent event = new DefaultScheduleEvent();

	public ScheduleController() {
		eventModel = new DefaultScheduleModel();
		/*
		 * eventModel.addEvent(new DefaultScheduleEvent("20,10",
		 * previousDay8Pm(), previousDay11Pm())); eventModel.addEvent(new
		 * DefaultScheduleEvent("35,50", today1Pm(), today6Pm()));
		 * eventModel.addEvent(new DefaultScheduleEvent("45,50", nextDay9Am(),
		 * nextDay11Am())); eventModel.addEvent(new
		 * DefaultScheduleEvent("25,30", theDayAfter3Pm(), fourDaysLater3pm()));
		 */
		eventModel.addEvent(new DefaultScheduleEvent("20,10", june158am(),
				june159am()));
		eventModel.addEvent(new DefaultScheduleEvent("35,15", june159am(),
				june1510am()));
		eventModel.addEvent(new DefaultScheduleEvent("45,25", june1510am(),
				june1511am()));
		

	}

	public Date getRandomDate(Date base) {
		Calendar date = Calendar.getInstance();
		date.setTime(base);
		date.add(Calendar.DATE, ((int) (Math.random() * 30)) + 1); // set random
																	// day of
																	// month

		return date.getTime();
	}

	public Date getInitialDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(calendar.get(Calendar.YEAR), Calendar.FEBRUARY,
				calendar.get(Calendar.DATE), 0, 0, 0);

		return calendar.getTime();
	}

	public ScheduleModel getEventModel() {
		return eventModel;
	}

	private Calendar today() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DATE), 0, 0, 0);

		return calendar;
	}

	private Date previousDay8Pm() {
		Calendar t = (Calendar) today().clone();
		t.set(Calendar.AM_PM, Calendar.PM);
		t.set(Calendar.DATE, t.get(Calendar.DATE) - 1);
		t.set(Calendar.HOUR, 8);

		return t.getTime();
	}

	private Date june156am() {
		Calendar t = Calendar.getInstance();
		t.set(Calendar.DATE, 15);
		t.set(Calendar.MONTH, Calendar.JUNE);
		t.set(Calendar.YEAR, 2014);
		t.set(Calendar.AM_PM, Calendar.AM);
		t.set(Calendar.HOUR, 6);

		return t.getTime();
	}

	private Date june157am() {
		Calendar t = Calendar.getInstance();
		t.set(Calendar.DATE, 15);
		t.set(Calendar.MONTH, Calendar.JUNE);
		t.set(Calendar.YEAR, 2014);
		t.set(Calendar.AM_PM, Calendar.AM);
		t.set(Calendar.HOUR, 7);
		t.set(Calendar.MINUTE, 0);

		return t.getTime();
	}

	private Date june158am() {
		Calendar t = Calendar.getInstance();
		t.set(Calendar.DATE, 15);
		t.set(Calendar.MONTH, Calendar.JUNE);
		t.set(Calendar.YEAR, 2014);
		t.set(Calendar.AM_PM, Calendar.AM);
		t.set(Calendar.HOUR, 8);
		t.set(Calendar.MINUTE, 0);

		return t.getTime();
	}

	private Date june159am() {
		Calendar t = Calendar.getInstance();
		t.set(Calendar.DATE, 15);
		t.set(Calendar.MONTH, Calendar.JUNE);
		t.set(Calendar.YEAR, 2014);
		t.set(Calendar.AM_PM, Calendar.AM);
		t.set(Calendar.HOUR, 9);
		t.set(Calendar.MINUTE, 0);

		return t.getTime();
	}

	private Date june1510am() {
		Calendar t = Calendar.getInstance();
		t.set(Calendar.DATE, 15);
		t.set(Calendar.MONTH, Calendar.JUNE);
		t.set(Calendar.YEAR, 2014);
		t.set(Calendar.AM_PM, Calendar.AM);
		t.set(Calendar.HOUR, 10);
		t.set(Calendar.MINUTE, 0);

		return t.getTime();
	}

	private Date june1511am() {
		Calendar t = Calendar.getInstance();
		t.set(Calendar.DATE, 15);
		t.set(Calendar.MONTH, Calendar.JUNE);
		t.set(Calendar.YEAR, 2014);
		t.set(Calendar.AM_PM, Calendar.AM);
		t.set(Calendar.HOUR, 1);
		t.set(Calendar.MINUTE, 0);

		return t.getTime();
	}

	private Date june1512pm() {
		Calendar t = Calendar.getInstance();
		t.set(Calendar.DATE, 15);
		t.set(Calendar.MONTH, Calendar.JUNE);
		t.set(Calendar.YEAR, 2014);
		t.set(Calendar.AM_PM, Calendar.PM);
		t.set(Calendar.HOUR, 12);
		t.set(Calendar.MINUTE, 0);

		return t.getTime();
	}

	private Date june151pm() {
		Calendar t = Calendar.getInstance();
		t.set(Calendar.DATE, 15);
		t.set(Calendar.MONTH, Calendar.JUNE);
		t.set(Calendar.YEAR, 2014);
		t.set(Calendar.AM_PM, Calendar.PM);
		t.set(Calendar.HOUR, 1);
		t.set(Calendar.MINUTE, 0);

		return t.getTime();
	}

	private Date previousDay11Pm() {
		Calendar t = (Calendar) today().clone();
		t.set(Calendar.AM_PM, Calendar.PM);
		t.set(Calendar.DATE, t.get(Calendar.DATE) - 1);
		t.set(Calendar.HOUR, 11);

		return t.getTime();
	}

	private Date today1Pm() {
		Calendar t = (Calendar) today().clone();
		t.set(Calendar.AM_PM, Calendar.PM);
		t.set(Calendar.HOUR, 1);

		return t.getTime();
	}

	private Date theDayAfter3Pm() {
		Calendar t = (Calendar) today().clone();
		t.set(Calendar.DATE, t.get(Calendar.DATE) + 2);
		t.set(Calendar.AM_PM, Calendar.PM);
		t.set(Calendar.HOUR, 3);

		return t.getTime();
	}

	private Date today6Pm() {
		Calendar t = (Calendar) today().clone();
		t.set(Calendar.AM_PM, Calendar.PM);
		t.set(Calendar.HOUR, 6);

		return t.getTime();
	}

	private Date nextDay9Am() {
		Calendar t = (Calendar) today().clone();
		t.set(Calendar.AM_PM, Calendar.AM);
		t.set(Calendar.DATE, t.get(Calendar.DATE) + 1);
		t.set(Calendar.HOUR, 9);

		return t.getTime();
	}

	private Date nextDay11Am() {
		Calendar t = (Calendar) today().clone();
		t.set(Calendar.AM_PM, Calendar.AM);
		t.set(Calendar.DATE, t.get(Calendar.DATE) + 1);
		t.set(Calendar.HOUR, 11);

		return t.getTime();
	}

	private Date fourDaysLater3pm() {
		Calendar t = (Calendar) today().clone();
		t.set(Calendar.AM_PM, Calendar.PM);
		t.set(Calendar.DATE, t.get(Calendar.DATE) + 4);
		t.set(Calendar.HOUR, 3);

		return t.getTime();
	}

	public ScheduleEvent getEvent() {
		return event;
	}

	public void setEvent(ScheduleEvent event) {
		this.event = event;
	}

	public void addEvent(ActionEvent actionEvent) {
		if (event.getId() == null)
			eventModel.addEvent(event);
		else
			eventModel.updateEvent(event);

		event = new DefaultScheduleEvent();
	}

	public void onEventSelect(SelectEvent selectEvent) {
		event = (ScheduleEvent) selectEvent.getObject();
	}

	public void onDateSelect(SelectEvent selectEvent) {
		event = new DefaultScheduleEvent("", (Date) selectEvent.getObject(),
				(Date) selectEvent.getObject());
	}

	public void onEventMove(ScheduleEntryMoveEvent event) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
				"Event moved", "Day delta:" + event.getDayDelta()
						+ ", Minute delta:" + event.getMinuteDelta());

		addMessage(message);
	}

	public void onEventResize(ScheduleEntryResizeEvent event) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
				"Event resized", "Day delta:" + event.getDayDelta()
						+ ", Minute delta:" + event.getMinuteDelta());

		addMessage(message);
	}

	private void addMessage(FacesMessage message) {
		FacesContext.getCurrentInstance().addMessage(null, message);
	}
}
