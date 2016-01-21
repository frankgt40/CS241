package edu.uci.ccai6;
 
import java.lang.annotation.*;

@Documented
public @interface ClassMetaData {
	String author();
	String date();
	int currentRevision() default 1;
	String lastModified() default "N/A";
	String lastModifiedBy() default "N/A";
	String[] reviewers();
}