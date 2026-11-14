export type Role = 'CUSTOMER' | 'OPERATOR' | 'ADMIN';

export type VehicleStatus = 'AVAILABLE' | 'IN_TEST_DRIVE' | 'MAINTENANCE' | 'RESERVED';

export type FuelType = 'GASOLINE' | 'ETHANOL' | 'FLEX' | 'DIESEL' | 'ELECTRIC' | 'HYBRID';

export type TransmissionType = 'MANUAL' | 'AUTOMATIC' | 'CVT' | 'AUTOMATED';

export type TestDriveStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'COMPLETED' | 'CANCELED';

export interface User {
  id: number;
  name: string;
  email: string;
  role: Role;
  active: boolean;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  user: User;
}

export interface Brand {
  id: number;
  name: string;
  countryOfOrigin: string;
  active: boolean;
}

export interface Category {
  id: number;
  name: string;
  description: string;
}

export interface Branch {
  id: number;
  name: string;
  cnpj: string;
  city: string;
  state: string;
  address: string;
  maxDailyTestDrives: number;
}

export interface VehicleImage {
  id: number;
  url: string;
  isPrimary: boolean;
}

export interface VehicleFeature {
  id: number;
  name: string;
  description: string;
}

export interface Vehicle {
  id: number;
  model: string;
  yearManufacture: number;
  yearModel: number;
  price: number;
  mileage: number;
  color: string;
  fuelType: FuelType;
  transmission: TransmissionType;
  chassisNumber: string;
  status: VehicleStatus;
  brand: Brand;
  category: Category;
  branch: Branch;
  images: VehicleImage[];
  features: VehicleFeature[];
}

export interface VehicleEligibility {
  vehicleId: number;
  model: string;
  classification: string;
  depreciationRate: number;
  estimatedInsurancePerDay: number;
  isEligibleForTestDrive: boolean;
  displayEligibilityScore: number;
  criteriaEvaluated: string[];
}

export interface CustomerProfile {
  id: number;
  userId: number;
  name: string;
  email: string;
  cpf: string;
  phone: string;
  cnhNumber: string;
  cnhExpirationDate: string;
  birthDate: string;
}

export interface Feedback {
  id: number;
  rating: number;
  comments: string;
  interestedInBuying: boolean;
  appointmentId: number;
}

export interface TestDriveAppointment {
  id: number;
  scheduledDateTime: string;
  status: TestDriveStatus;
  customerNotes?: string;
  operatorNotes?: string;
  customer: CustomerProfile;
  vehicle: Vehicle;
  approvedByOperatorName?: string;
  feedback?: Feedback;
}

export interface Operator {
  id: number;
  userId: number;
  name: string;
  email: string;
  registrationCode: string;
  hireDate: string;
  department: string;
  branchId: number;
  branchName: string;
  active: boolean;
}

export interface AuditLog {
  id: number;
  action: string;
  entityName: string;
  entityId: string;
  performedByEmail: string;
  timestamp: string;
  details: string;
}

