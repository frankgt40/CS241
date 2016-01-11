package com.uci.ccai6;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.uci.ccai6.exception.ParserException;


public class ParserTest {

	PrintWriter writer = null;
	String fileName = "ParserTest.txt";
	Lexer lex = null;
	
	@Before
	public void setUp() throws Exception {
		writer = new PrintWriter(fileName, "UTF-8");
	}
	
	@After
	public void tearDown() {
		writer.close();
		new File(fileName).delete();
		writer = null;
	}
	
	boolean testIdentHelper(String s) throws FileNotFoundException, UnsupportedEncodingException {
		writer = new PrintWriter(fileName, "UTF-8");
		writer.println(s);
		writer.close();
		lex = new Lexer(fileName);
		Parser parser = new Parser(lex);
		
		try {
			parser.ident();
			return true;
		} catch(Exception e) {
			parser.next();
			return false;
		}
		
	}
	
	@Test
	public void testIdent() throws Exception {
		assertTrue(testIdentHelper("abc"));
		assertFalse(testIdentHelper("A"));
		assertFalse(testIdentHelper("cAe"));
		assertTrue(testIdentHelper("a"));
		assertTrue(testIdentHelper("a4"));
		assertFalse(testIdentHelper("4"));
		
	}
	

	boolean testNumberHelper(String s) throws FileNotFoundException, UnsupportedEncodingException {
		writer = new PrintWriter(fileName, "UTF-8");
		writer.println(s);
		writer.close();
		lex = new Lexer(fileName);
		Parser parser = new Parser(lex);
		
		try {
			parser.number();
			return true;
		} catch(Exception e) {
			parser.next();
			return false;
		}
		
	}
	
	@Test
	public void testNumber() throws Exception {
		assertTrue(testNumberHelper("1234577"));
		assertTrue(testNumberHelper("2"));
		assertTrue(testNumberHelper("999"));
		assertFalse(testNumberHelper("a"));
		assertFalse(testNumberHelper("12A"));
		assertFalse(testNumberHelper("3a"));
	}

	boolean testDesignatorHelper(String s) throws FileNotFoundException, UnsupportedEncodingException {
		writer = new PrintWriter(fileName, "UTF-8");
		writer.println(s + "\n}");
		writer.close();
		lex = new Lexer(fileName);
		Parser parser = new Parser(lex);
		
		try {
			parser.designator();
			return true;
		} catch(Exception e) {
			parser.next();
			return false;
		}
		
	}
	
	@Test
	public void testDesignator() throws Exception {
		// TODO: test with funcCall
		assertTrue(testDesignatorHelper("abdc"));
		assertTrue(testDesignatorHelper("abc[5]"));
		assertTrue(testDesignatorHelper("a[3+x]"));
		assertTrue(testDesignatorHelper("a[3*y+x]"));
		assertTrue(testDesignatorHelper("a[ab[4+s]*3+x]"));
		assertFalse(testDesignatorHelper("a[1)"));
		assertFalse(testDesignatorHelper("123[1]"));
		assertFalse(testDesignatorHelper("aa[x<5]"));
	}

	boolean testFactorHelper(String s) throws FileNotFoundException, UnsupportedEncodingException {
		writer = new PrintWriter(fileName, "UTF-8");
		writer.println(s + "\n}");
		writer.close();
		lex = new Lexer(fileName);
		Parser parser = new Parser(lex);
		
		try {
			parser.factor();
			return true;
		} catch(Exception e) {
			parser.next();
			return false;
		}
		
	}
	
	@Test
	public void testFactor() throws Exception {
		// TODO: test with funcCall
		assertTrue(testFactorHelper("a[ab[4+s]*3+x]"));
		assertTrue(testFactorHelper("12351"));
		assertTrue(testFactorHelper("(2+5*c)"));
		assertTrue(testFactorHelper("(a)"));
		assertFalse(testFactorHelper("(a]"));
		assertFalse(testFactorHelper("[a]"));
		assertFalse(testFactorHelper("<"));
	}

	boolean testRelationHelper(String s) throws FileNotFoundException, UnsupportedEncodingException {
		writer = new PrintWriter(fileName, "UTF-8");
		writer.println(s + "\n}");
		writer.close();
		lex = new Lexer(fileName);
		Parser parser = new Parser(lex);
		
		try {
			parser.relation();
			return true;
		} catch(Exception e) {
			System.out.println(e.getMessage());
			parser.next();
			return false;
		}
		
	}
	
	@Test
	public void testRelation() throws Exception {
		// TODO: test with funcCall
		assertTrue(testRelationHelper("a < b"));
		assertTrue(testRelationHelper("123 >= xy"));
		assertTrue(testRelationHelper("(5+2)!=5+2"));
		assertTrue(testRelationHelper("3==x*y+z"));
		assertFalse(testRelationHelper("3<-a"));
		assertFalse(testRelationHelper("x=5"));
	}
}
