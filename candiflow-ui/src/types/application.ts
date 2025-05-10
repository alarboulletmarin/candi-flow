export interface ApplicationStatus {
  id: string; // UUID
  name: string;
  description: string;
  color: string;
  order: number;
}

export interface StatusUpdate {
  id: string; // UUID
  applicationId: string; // UUID
  statusId: string; // UUID
  status: ApplicationStatus;
  notes: string;
  createdAt: string;
  updatedAt: string;
  interviewDate?: string; // Ajout du champ interviewDate qui semble être utilisé
}

export interface Application {
  id: string; // UUID
  userId: string; // UUID
  company: string;
  position: string;
  location: string;
  description: string;
  applicationDate: string;
  contactPerson: string;
  contactEmail: string;
  salary: string;
  notes: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  source: string;
  url: string;
  createdAt: string;
  updatedAt: string;
  currentStatus?: ApplicationStatus;
  statusUpdates?: StatusUpdate[];
}

export interface ApplicationFormData {
  company: string;
  position: string;
  location: string;
  description: string;
  applicationDate: string;
  contactPerson: string;
  contactEmail: string;
  salary: string;
  notes: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  source: string;
  url: string;
}

export interface StatusUpdateFormData {
  statusId: number;
  notes: string;
}
