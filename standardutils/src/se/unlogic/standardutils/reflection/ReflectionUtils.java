/*******************************************************************************
 * Copyright (c) 2010 Robert "Unlogic" Olofsson (unlogic@unlogic.se).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0-standalone.html
 ******************************************************************************/
package se.unlogic.standardutils.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import se.unlogic.standardutils.populators.BeanStringPopulator;
import se.unlogic.standardutils.populators.BeanStringPopulatorRegistery;
import se.unlogic.standardutils.populators.UnableToFindSuitablePopulatorException;
import se.unlogic.standardutils.string.StringUtils;

public class ReflectionUtils {

	public static Object getInstance(String className) throws NoClassDefFoundError, ClassNotFoundException, InstantiationException, IllegalAccessException {
		return Class.forName(className).newInstance();
	}

	public static boolean isGenericlyTyped(Field field) {

		if (field.getGenericType() instanceof ParameterizedType) {

			return true;
		}

		return false;
	}

	public static int getGenericlyTypeCount(Field field) {

		if (field.getGenericType() instanceof ParameterizedType) {

			ParameterizedType type = (ParameterizedType) field.getGenericType();

			return type.getActualTypeArguments().length;
		}

		return 0;
	}

	public static int getGenericlyTypeCount(Method method) {

		return method.getGenericParameterTypes().length;
	}

	public static boolean checkGenericTypes(Field field, Class<?>... classes) {

		if (field.getGenericType() instanceof ParameterizedType) {

			ParameterizedType type = (ParameterizedType) field.getGenericType();

			if (type.getActualTypeArguments().length != classes.length) {
				return false;
			}

			for (int i = 0; i < classes.length; i++) {

				if (!type.getActualTypeArguments()[i].equals(classes[i])) {
					return false;
				}
			}

			return true;
		}

		return false;
	}

	public static Type getGenericType(Field field) {

		Type type = ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];

		if(type instanceof WildcardType){

			//TODO do a little bit more research on this part...
			return ((WildcardType)type).getUpperBounds()[0];

		}else{

			return type;
		}
	}

	public static Object getGenericType(Method method) {

		return method.getGenericParameterTypes()[0];
	}

	public static void fixFieldAccess(Field field) {

		if(!field.isAccessible()){
			field.setAccessible(true);
		}
	}

	public static void fixMethodAccess(Method method) {

		if(!method.isAccessible()){
			method.setAccessible(true);
		}
	}

	public static Field getField(Class<?> bean, String fieldName) {


		List<Field> fields = getFields(bean);

		for(Field field : fields){

			if(field.getName().equals(fieldName)){

				return field;
			}
		}

		throw new RuntimeException(new NoSuchFieldError(fieldName));
	}

	public static List<Field> getFields(Class<?> bean, String... fieldNames) {

		List<Field> fields = getFields(bean);

		List<Field> matchingFields = new ArrayList<>(fieldNames.length);
		
		outer: for(String fieldName : fieldNames) {
		
			for(Field field : fields){

				if(field.getName().equals(fieldName)){

					matchingFields.add(field);
					continue outer;
				}
			}
			
			throw new RuntimeException(new NoSuchFieldError(fieldName));
		}

		return matchingFields;
	}
	
	public static boolean isAvailable(String classname) {
		try {
			Class.forName(classname);
			return true;
		} catch (Throwable t) {
			return false;
		}
	}

	public static List<Field> getFields(Class<?> clazz){

		ArrayList<Field> fields = new ArrayList<Field>();

		if(clazz.isInterface()){

			return fields;
		}

		fields.addAll(Arrays.asList(clazz.getDeclaredFields()));

		clazz = clazz.getSuperclass();

		while(clazz != null){

			fields.addAll(Arrays.asList(clazz.getDeclaredFields()));

			clazz = clazz.getSuperclass();
		}

		return fields;
	}

	public static List<Method> getMethods(Class<?> clazz){

		ArrayList<Method> methods = new ArrayList<Method>();

		methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));

		clazz = clazz.getSuperclass();

		while(clazz != null){

			methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));

			clazz = clazz.getSuperclass();
		}

		return methods;
	}

	public static Method getMethod(Class<?> clazz, String methodName, Class<?> returnType, Class<?>... inputParams) {

		if(inputParams == null){

			inputParams = new Class<?>[0];
		}

		Method[] methods = clazz.getDeclaredMethods();

		for(Method method : methods){

			if(method.getName().equals(methodName) && returnType.isAssignableFrom(method.getReturnType()) && Arrays.equals(inputParams, method.getParameterTypes())){

				return method;
			}
		}

		return null;
	}


	public static void setFieldValue(Field field, Object value, Object target) throws IllegalArgumentException, IllegalAccessException {

		boolean declaredAccessible = field.isAccessible();

		if (!declaredAccessible) {
			field.setAccessible(true);
		}

		field.set(target, value);

		if (!declaredAccessible) {
			field.setAccessible(false);
		}
	}

	public static void setMethodValue(Method method, Object value, Object target) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		boolean declaredAccessible = method.isAccessible();

		if (!declaredAccessible) {
			method.setAccessible(true);
		}

		method.invoke(target, value);

		if (!declaredAccessible) {
			method.setAccessible(false);
		}
	}

	public static boolean setSetterMethod(Object target, String methodName, String value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, MethodNotFoundException, UnableToFindSuitablePopulatorException {

		if(!methodName.startsWith("set")){

			methodName = "set" + StringUtils.toFirstLetterUppercase(methodName);
		}

		Method method = getMethod(target.getClass(),methodName,1);

		if(method == null){

			throw new MethodNotFoundException("Unable to find setter method " + methodName + " with correct signature");
		}

		if(value == null || method.getParameterTypes()[0].equals(String.class)){

			setMethodValue(method,value,target);
		}

		BeanStringPopulator<?> populator = BeanStringPopulatorRegistery.getBeanStringPopulator(method.getParameterTypes()[0]);

		if(populator == null){

			throw new UnableToFindSuitablePopulatorException("Unable to find BeanStringPopulator for " + method.getParameterTypes()[0]);
		}

		setMethodValue(method,populator.getValue(value),target);

		return true;
	}

	public static Method getMethod(Class<?> clazz, String methodName, int argumentCount) {

		List<Method> methods = getMethods(clazz);

		for(Method method : methods){

			if(!method.getName().equalsIgnoreCase(methodName)){

				continue;
			}

			if(method.getParameterTypes().length != argumentCount){

				continue;
			}

			return method;
		}

		return null;
	}

	public static Object getMethodValue(String methodName, Object instance) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		List<Method> methods = getMethods(instance.getClass());

		for(Method method : methods){

			if(!method.getName().equalsIgnoreCase(methodName)){

				continue;
			}

			if(method.getParameterTypes().length != 0){

				continue;
			}

			method.setAccessible(true);
			
			return method.invoke(instance);
		}

		return null;
	}	
	
	public static<T> T getInstance(Class<T> clazz) {

		try{
			return clazz.newInstance();

		}catch(InstantiationException e){

			throw new RuntimeException(e);

		}catch(IllegalAccessException e){

			throw new RuntimeException(e);
		}
	}

	public static void printFields(Object object){

		printFields(object, 1);
	}
	
	public static void printFields(Object object, int indentation){

		if(object == null){
			
			return;
		}
		
		if(object instanceof Collection<?>){
			
			for(Object o : (Collection<?>)object){
				
				printFields(o, indentation);
			}
		
		}else{
		
			List<Field> fields = getFields(object.getClass());

			for (int i = 1; i < indentation; i++) {
				System.out.print(" ");
			}
			
			System.out.println(object.getClass().getSimpleName());
			
			try{
				for(Field field : fields){

					fixFieldAccess(field);
					
					for (int i = 0; i < indentation; i++) {
						System.out.print(" ");
					}

					System.out.println(field.getName() + ": " + field.get(object));
				}

			}catch(IllegalArgumentException e){

				throw new RuntimeException(e);

			}catch(IllegalAccessException e){

				throw new RuntimeException(e);
			}
			
			System.out.println("");			
		}
	}
}
