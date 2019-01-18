package com.avioconsulting.mule.testing

import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar
import java.time.ZonedDateTime

trait XmlDateHelp {
    XMLGregorianCalendar getXmlDate(int year, int oneBasedMonth, int dayOfMonth) {
        def zeroBasedMonth = oneBasedMonth - 1
        def gregorian = new GregorianCalendar(year,
                                              zeroBasedMonth,
                                              dayOfMonth)
        DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorian)
    }

    XMLGregorianCalendar getXmlDateTime(int year, int oneBasedMonth, int dayOfMonth, int hourOfDay, int minute,
                                        int second = 0, String timeZoneId) {
        def zeroBasedMonth = oneBasedMonth - 1
        def gregorian = new GregorianCalendar(year,
                                              zeroBasedMonth,
                                              dayOfMonth,
                                              hourOfDay,
                                              minute,
                                              second)
        gregorian.setTimeZone(TimeZone.getTimeZone(timeZoneId))
        DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorian)
    }

    XMLGregorianCalendar getXmlDateTime(ZonedDateTime dateTime) {
        def zeroBasedMonth = dateTime.monthValue - 1
        def gregorian = new GregorianCalendar(dateTime.year,
                                              zeroBasedMonth,
                                              dateTime.dayOfMonth,
                                              dateTime.hour,
                                              dateTime.minute,
                                              dateTime.second)
        gregorian.setTimeZone(TimeZone.getTimeZone(dateTime.zone))
        DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorian)
    }
}
