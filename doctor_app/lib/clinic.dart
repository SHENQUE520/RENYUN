import 'package:flutter/material.dart';

import 'roster.dart';

class Clinic extends ChangeNotifier {
  String selectedId = 'p1';
  final List<ClinicPatient> _extra = [];

  List<ClinicPatient> get patients {
    final known = roster.map((p) => p.id).toSet();
    return [...roster, ..._extra.where((p) => !known.contains(p.id))];
  }

  ClinicPatient get selected {
    return patients.firstWhere((p) => p.id == selectedId, orElse: () => patients.first);
  }

  int get warningPatients => patients.where((p) => warnCount(p) > 0).length;

  void select(String id) {
    selectedId = id;
    notifyListeners();
  }

  void remember(ClinicPatient patient) {
    if (patients.every((p) => p.id != patient.id)) _extra.add(patient);
    selectedId = patient.id;
    notifyListeners();
  }
}
