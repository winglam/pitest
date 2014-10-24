/*
 * Copyright 2010 Henry Coles
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and limitations under the License. 
 */

package org.pitest.mutationtest.engine.gregor.mutators;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;

import java.util.List;
import java.util.concurrent.Callable;

import static java.util.Arrays.asList;
import static org.pitest.mutationtest.engine.gregor.mutators.ReplaceMethodWithParameterOfSameTypeAsReturnValueMutator.REPLACE_METHOD_WITH_PARAMETER_OF_SAME_TYPE_AS_RETURN_VALUE_MUTATOR;

public class ReplaceMethodWithParameterOfSameTypeAsReturnValueMutatorTest
    extends MutatorTestBase {

  @Before
  public void setupEngineToUseReplaceMethodWithParameterOfSameTypeAsReturnValueMutator() {
    createTesteeWith(mutateOnlyCallMethod(),
        REPLACE_METHOD_WITH_PARAMETER_OF_SAME_TYPE_AS_RETURN_VALUE_MUTATOR);
  }

  @Test
  public void shouldReplaceMethodCallWithStringArgument() throws Exception {
    final Mutant mutant = getFirstMutant(HasStringMethodCall.class);
    assertMutantCallableReturns(new HasStringMethodCall("example"), mutant,
        "example");
  }

  private static class HasStringMethodCall implements Callable<String> {
    private String arg;

    public HasStringMethodCall(String arg) {
      this.arg = arg;
    }

    public String delegate(final String aString) {
      return "abc" + aString;
    }

    public String call() throws Exception {
      return delegate(arg);
    }
  }

  @Test
  public void shouldReplaceMethodCallWithIntArgument() throws Exception {
    final Mutant mutant = getFirstMutant(HasIntMethodCall.class);
    assertMutantCallableReturns(new HasIntMethodCall(20), mutant, "20");
  }

  private static class HasIntMethodCall implements Callable<String> {
    private int arg;

    public HasIntMethodCall(int arg) {
      this.arg = arg;
    }

    public int delegate(int aInt) {
      return 22 + aInt;
    }

    public String call() throws Exception {
      return String.valueOf(delegate(arg));
    }
  }

  @Test
  public void shouldReplaceMethodCallWithLongArgument() throws Exception {
    final Mutant mutant = getFirstMutant(HasLongMethodCall.class);
    assertMutantCallableReturns(new HasLongMethodCall(20L), mutant, "20");
  }

  private static class HasLongMethodCall implements Callable<String> {
    private long arg;

    public HasLongMethodCall(long arg) {
      this.arg = arg;
    }

    public long delegate(long parameter) {
      return 22L + parameter;
    }

    public String call() throws Exception {
      return String.valueOf(delegate(arg));
    }
  }

  @Test
  public void doesNotMutationMethodThatReturnsDifferentType() throws Exception {
    assertNoMutants(ReturnsDifferentType.class);
  }

  private class ReturnsDifferentType {
    public String aMethodReturningADifferentTypeThanItsArgument(Object arg) {
      return String.valueOf(arg);
    }
  }

  @Test
  public void continuesUntilMatchingArgumentTypeIsFound() throws Exception {
    Mutant mutant = getFirstMutant(OnlyFirstArgumentHasMatchingType.class);
    assertMutantCallableReturns(
        new OnlyFirstArgumentHasMatchingType("abc", new Object(), 3), mutant,
        "abc");
  }

  private class OnlyFirstArgumentHasMatchingType implements Callable<String> {
    private String aString;
    private Object anObject;
    private long   aLong;

    public OnlyFirstArgumentHasMatchingType(String aString, Object anObject,
        long aLong) {
      this.aString = aString;
      this.anObject = anObject;
      this.aLong = aLong;
    }

    @Override
    public String call() throws Exception {
      return aMethod(aString, anObject, aLong);
    }

    private String aMethod(String aString, Object anObject, long aLong) {
      return String.valueOf(anObject) + aString + String.valueOf(aLong);
    }
  }

  @Test
  public void usesLastArgumentOfMatchingTypeToReplaceMethod() throws Exception {
    Mutant mutant = getFirstMutant(HasSeveralArgumentWithMatchingType.class);
    assertMutantCallableReturns(new HasSeveralArgumentWithMatchingType(11, 22),
        mutant, "22");
  }

  private class HasSeveralArgumentWithMatchingType implements Callable<String> {
    private int int1;
    private int int2;

    public HasSeveralArgumentWithMatchingType(int i, int j) {
      this.int1 = i;
      this.int2 = j;
    }

    @Override
    public String call() throws Exception {
      String anInt = "3";
      return String.valueOf(aMethod(int1, anInt, int2));
    }

    private int aMethod(int int1, String aString, int int2) {
      return int1 + int2;
    }
  }

  @Test
  public void alsoReplaceCallToMethodWhenReturnValueIsNotUsed()
      throws Exception {
    Mutant mutant = getFirstMutant(ReturnValueNotUsed.class);
    assertMutantCallableReturns(new ReturnValueNotUsed(), mutant, false);
  }

  private class ReturnValueNotUsed implements Callable<Boolean> {
    private List<String> aList = asList("xyz");

    @Override
    public Boolean call() throws Exception {
      aList.set(0, "will not be present in list in mutated version");
      return aList.contains("will not be present in list in mutated version");
    }
  }

}