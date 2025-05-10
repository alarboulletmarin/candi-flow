import React, { useState } from 'react';
import { 
  StyleSheet, 
  TextInput as RNTextInput, 
  View, 
  Text, 
  TextInputProps as RNTextInputProps,
  TouchableOpacity
} from 'react-native';
import { Controller, Control, FieldValues, FieldPath } from 'react-hook-form';
import { Ionicons } from '@expo/vector-icons';

interface TextInputProps extends RNTextInputProps {
  label?: string;
  error?: string;
  name: string;
  control: Control<any>;
  isPassword?: boolean;
  leftIcon?: React.ReactNode;
}

export default function TextInput({
  label,
  error,
  name,
  control,
  isPassword = false,
  leftIcon,
  ...props
}: TextInputProps) {
  const [isFocused, setIsFocused] = useState(false);
  const [passwordVisible, setPasswordVisible] = useState(false);

  const togglePasswordVisibility = () => {
    setPasswordVisible(!passwordVisible);
  };

  return (
    <View style={styles.container}>
      {label && <Text style={styles.label}>{label}</Text>}
      
      <Controller
        control={control}
        name={name}
        render={({ field: { onChange, onBlur, value } }) => (
          <View style={[
            styles.inputContainer,
            isFocused && styles.inputFocused,
            error ? styles.inputError : null,
          ]}>
            {leftIcon && (
              <View style={styles.leftIconContainer}>
                {leftIcon}
              </View>
            )}
            
            <RNTextInput
              style={[
                styles.input,
                leftIcon ? styles.inputWithLeftIcon : null,
                isPassword && !passwordVisible ? styles.inputWithRightIcon : null,
              ]}
              value={value}
              onChangeText={onChange}
              onBlur={() => {
                setIsFocused(false);
                onBlur();
              }}
              onFocus={() => setIsFocused(true)}
              secureTextEntry={isPassword && !passwordVisible}
              {...props}
            />
            
            {isPassword && (
              <TouchableOpacity 
                style={styles.rightIconContainer} 
                onPress={togglePasswordVisibility}
              >
                <Ionicons 
                  name={passwordVisible ? 'eye-off-outline' : 'eye-outline'} 
                  size={20} 
                  color="#6B7280" 
                />
              </TouchableOpacity>
            )}
          </View>
        )}
      />
      
      {error && (
        <Text style={styles.errorMessage}>{error}</Text>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    marginBottom: 16,
    width: '100%',
  },
  label: {
    fontSize: 14,
    fontWeight: '500',
    marginBottom: 6,
    color: '#374151',
  },
  inputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#D1D5DB',
    borderRadius: 8,
    backgroundColor: 'white',
  },
  inputFocused: {
    borderColor: '#2563EB',
  },
  inputError: {
    borderColor: '#EF4444',
  },
  input: {
    flex: 1,
    paddingVertical: 12,
    paddingHorizontal: 16,
    fontSize: 16,
    color: '#1F2937',
  },
  inputWithLeftIcon: {
    paddingLeft: 8,
  },
  inputWithRightIcon: {
    paddingRight: 40,
  },
  leftIconContainer: {
    paddingLeft: 12,
  },
  rightIconContainer: {
    position: 'absolute',
    right: 12,
    height: '100%',
    justifyContent: 'center',
  },
  errorMessage: {
    color: '#EF4444',
    fontSize: 12,
    marginTop: 4,
  },
});
